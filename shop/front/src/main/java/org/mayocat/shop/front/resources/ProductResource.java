package org.mayocat.shop.front.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mayocat.configuration.ConfigurationService;
import org.mayocat.configuration.general.GeneralSettings;
import org.mayocat.context.Execution;
import org.mayocat.image.model.Image;
import org.mayocat.image.model.Thumbnail;
import org.mayocat.image.store.ThumbnailStore;
import org.mayocat.model.Attachment;
import org.mayocat.shop.catalog.CatalogService;
import org.mayocat.shop.catalog.configuration.shop.CatalogSettings;
import org.mayocat.shop.catalog.model.Product;
import org.mayocat.shop.front.FrontBindingManager;
import org.mayocat.shop.front.bindings.BindingsContants;
import org.mayocat.shop.front.builder.ProductBindingBuilder;
import org.mayocat.shop.rest.annotation.ExistingTenant;
import org.mayocat.base.Resource;
import org.mayocat.shop.rest.views.FrontView;
import org.mayocat.store.AttachmentStore;
import org.mayocat.theme.Breakpoint;
import org.mayocat.theme.Theme;
import org.xwiki.component.annotation.Component;

/**
 * @version $Id$
 */
@Component("/product/")
@Path("/product/")
@Produces(MediaType.TEXT_HTML)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
@ExistingTenant
public class ProductResource extends AbstractFrontResource implements Resource, BindingsContants
{
    @Inject
    private ConfigurationService configurationService;

    @Inject
    private CatalogService catalogService;

    @Inject
    private FrontBindingManager bindingManager;

    @Inject
    private Provider<AttachmentStore> attachmentStore;

    @Inject
    private Provider<ThumbnailStore> thumbnailStore;

    @Inject
    private Execution execution;



    @Path("{slug}")
    @GET
    public FrontView getProduct(@PathParam("slug") String slug, @Context Breakpoint breakpoint,
            @Context UriInfo uriInfo)
    {
        final Product product = this.catalogService.findProductBySlug(slug);
        if (product == null) {
            return new FrontView("404", breakpoint);
        }

        FrontView result = new FrontView("product", breakpoint);

        Map<String, Object> bindings = bindingManager.getBindings(uriInfo.getPathSegments());

        bindings.put(PAGE_TITLE, product.getTitle());
        bindings.put(PAGE_DESCRIPTION, product.getDescription());

        final CatalogSettings configuration = (CatalogSettings)
                configurationService.getSettings(CatalogSettings.class);
        final GeneralSettings generalSettings = (GeneralSettings)
                configurationService.getSettings(GeneralSettings.class);

        Theme theme = this.execution.getContext().getTheme();

        List<Attachment> attachments = this.attachmentStore.get().findAllChildrenOf(product);
        List<Image> images = new ArrayList<Image>();
        for (Attachment attachment : attachments) {
            if (isImage(attachment)) {
                List<Thumbnail> thumbnails = thumbnailStore.get().findAll(attachment);
                Image image = new Image(attachment, thumbnails);
                images.add(image);
            }
        }

        ProductBindingBuilder builder = new ProductBindingBuilder(configuration, generalSettings, theme);
        Map<String, Object> productContext = builder.build(product, images);

        bindings.put("product", productContext);
        result.putBindings(bindings);

        return result;
    }
}
