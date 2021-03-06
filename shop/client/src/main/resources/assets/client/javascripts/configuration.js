'use strict'

angular.module('configuration', ['ngResource'])
    .factory('configurationService', function ($resource, $q) {

        var configuration,
            configurationResource = $resource("/api/1.0/configuration/gestalt"),
            settings,
            settingsResource = $resource("/api/1.0/configuration/settings", {}, {
                update:{method:"PUT"}
            });

        var getSettings = function () {
            var deferred = $q.defer();
            if (settings != null) {
                deferred.resolve(settings);
            }
            else {
                settingsResource.get(function (result) {
                    saveOriginalValues(result);
                    settings = result;
                    deferred.resolve(settings);
                });
            }
            return deferred.promise;
        };

        var getConfiguration = function () {
            var deferred = $q.defer();
            if (configuration != null) {
                deferred.resolve(configuration);
            }
            else {
                configurationResource.get(function (result) {
                    configuration = result;
                    deferred.resolve(configuration);
                });
            }
            return deferred.promise;
        };

        var isConfigurable = function (node) {
            return typeof node !== "undefined"
                && node !== null
                && typeof node.configurable !== "undefined"
                && typeof node.value !== "undefined"
                && typeof node.default !== "undefined"
                && typeof node.visible !== "undefined";
        }

        var saveOriginalValues = function (settings) {
            var walk = function (node) {
                for (var property in node) {
                    if (node.hasOwnProperty(property)) {
                        if (isConfigurable(node[property])) {
                            node[property].__originalValue = node[property].value;
                        }
                        else if (typeof node[property] === "object") {
                            // We need to go deeper...
                            walk(node[property]);
                        }
                        else {
                            // What do we do with properties that are not configurable ?
                            // Nothing for now. We need to see if we want to support them first.
                        }
                    }
                }
            }

            walk(settings);
        }

        var prepareSettings = function (settings) {

            var isStillDefaultValue = function (node) {
                if (node.value === node.__originalValue && node.value === node.defaultValue) {
                    // Nothing changed
                    return true;
                }
                return false;
            }

            var walk = function (node, container) {
                for (var property in node) {
                    if (node.hasOwnProperty(property)) {
                        if (isConfigurable(node[property])) {
                            if (!isStillDefaultValue(node[property])) {
                                container[property] = node[property].value;
                            }
                        }
                        else if (typeof node[property] === "object") {
                            // We need to go deeper...
                            container[property] = walk(node[property], {});
                        }
                        else {
                            // What do we do with properties that are not configurable ?
                            // Nothing for now. We need to see if we want to support them first.
                        }
                    }
                }
                return container;
            }

            return walk(settings, {});
        };

        return {
            /**
             * Gets access to either the whole configuration object, or to a single configuration property.
             *
             * To get access to the whole configuration object :
             *
             * configurationService.get(function(configuration){
             *   // Something with configuration
             * });
             *
             * To get access to a single configuration property :
             *
             * configurationService.get("module.sample.property", function(value){
             *   // Something with value
             * });
             */
            get:function () {
                var path = arguments.length === 2 ? arguments[0] : undefined,
                    callback = arguments.length === 2 ? arguments[1] : arguments[0];
                getConfiguration().then(function (configuration) {
                    if (typeof path === "undefined") {
                        callback(configuration);
                        return;
                    }
                    try {
                        var configurationElement = eval("configuration." + path);
                        if (typeof configurationElement === "undefined") {
                            // The configuration does not exist
                            callback && callback(undefined);
                        }
                        callback && callback(configurationElement);
                        return;
                    }
                    catch (error) {
                        callback && callback(undefined);
                        return;
                    }
                });
            },

            /**
             * Gets access to either the whole settings object, or to a single settings property.
             *
             * To get access to the whole settings object :
             *
             * configurationService.getSettings(function(settings){
             *   // Something something settings
             * });
             *
             * To get access to a single settings property :
             *
             * configurationService.get("module.sample.property", function(value){
             *   // Something with value
             * });
             */
            getSettings:function () {
                var path = arguments.length === 2 ? arguments[0] : undefined,
                    callback = arguments.length === 2 ? arguments[1] : arguments[0];
                getSettings().then(function (settings) {
                    if (typeof path === "undefined") {
                        callback(settings);
                        return;
                    }
                    try {
                        var setting = eval("settings." + path);
                        if (typeof setting === "undefined") {
                            // The configuration does not exist
                            callback && callback(undefined);
                        }
                        callback && callback(setting);
                        return;
                    }
                    catch (error) {
                        callback && callback(undefined);
                        return;
                    }
                });
            },

            /**
             * Updates the settings with the passed settings, hitting the /settings PUT API.
             *
             * @param {Object} config the settings object to put
             */
            put:function (config) {
                settingsResource.update(prepareSettings(settings));
            },

            /**
             * Checks if a settings property is visible (i.e. should be exposed to users) or not.
             *
             * @param {Object} settings the settings object to test a path for visibility for.
             * @param {String} path the settings path to test visibility for. For example: "general.locales.main"
             * @return {*} undefined if the settings does not exists at this path for this settings object,
             * false if the settings is not visible (i.e. it should not be exposed to the users), true if it is.
             */
            isVisible:function (settings, path) {
                if (typeof settings === "undefined") {
                    return;
                }
                var settingsElement = eval("settings." + path);
                if (typeof settingsElement === "undefined") {
                    // The settings does not exist
                    return;
                }
                return typeof settingsElement.visible === "undefined"
                    || settingsElement.visible;
            },

            /**
             * Checks if a settings property is configurable (users are allowed to override the value set at the
             * platform level) or not.
             *
             * @param {Object} settings the settings object to test a path for configurability for.
             * @param {String} path the settings path to test configurability for.
             * For example: "general.locales.main"
             * @return {Boolean|undefined} undefined if the settings does not exists at this path for this
             * settings object, false if the settings is not configurable, true if it is.
             */
            isConfigurable:function (settings, path) {
                if (typeof settings === "undefined") {
                    return undefined;
                }
                var settingsElement = eval("settings." + path);
                if (typeof settingsElement === "undefined") {
                    // The settings does not exist
                    return undefined;
                }
                return typeof settingsElement.configurable === "undefined"
                    || settingsElement.configurable;
            },

            /**
             * Checks if a settings property value is the default value (the one set at the platform level).
             *
             * @param {Object} settings the settings object to check the default value with
             * @param {String} path the path of the settings to check if the value is the default one for
             * @return {Boolean|undefined} undefined if the settings does not exists at this path for this
             * settings object, true if the value for this settings path is the default one, false otherwise
             */
            isDefaultValue:function (settings, path) {
                if (typeof settings === "undefined") {
                    return undefined;
                }
                var settingsElement = eval("settings." + path);
                if (typeof settingsElement === "undefined") {
                    // The settings does not exist
                    return undefined;
                }
                return typeof settingsElement.default !== "undefined"
                    && angular.equals(settingsElement.default, settingsElement.value);

            }
        };
    })
    .controller('ConfigurationController', ['$scope', 'configurationService',

    function ($scope, configurationService) {

        $scope.updateSettings = function () {
            configurationService.put($scope.settings);
        };

        $scope.isVisible = function (path) {
            return configurationService.isVisible($scope.settings, path);
        }

        $scope.isConfigurable = function (path) {
            return configurationService.isConfigurable($scope.settings, path);
        }

        $scope.isDefaultValue = function (path) {
            return configurationService.isDefaultValue($scope.confsettingsiguration, path);
        };

        configurationService.getSettings(function (settings) {
            $scope.settings = settings;
        });
    }

]);
