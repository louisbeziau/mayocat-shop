package org.mayocat.shop.catalog.internal;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.mayocat.Slugifier;
import org.mayocat.shop.catalog.model.Category;
import org.mayocat.shop.catalog.store.CategoryStore;
import org.mayocat.shop.catalog.store.ProductStore;
import org.mayocat.shop.catalog.model.Product;
import org.mayocat.model.EntityAndCount;
import org.mayocat.shop.catalog.CatalogService;
import org.mayocat.store.EntityAlreadyExistsException;
import org.mayocat.store.EntityDoesNotExistException;
import org.mayocat.store.HasOrderedCollections;
import org.mayocat.store.InvalidEntityException;
import org.mayocat.store.InvalidMoveOperation;
import org.mayocat.store.InvalidOperation;
import org.xwiki.component.annotation.Component;

import com.google.common.base.Strings;

@Component
public class DefaultCatalogService implements CatalogService
{
    @Inject
    private Provider<ProductStore> productStore;

    @Inject
    private Provider<CategoryStore> categoryStore;

    @Inject
    private Slugifier slugifier;

    public Product createProduct(Product entity) throws InvalidEntityException, EntityAlreadyExistsException
    {
        if (Strings.isNullOrEmpty(entity.getSlug())) {
            entity.setSlug(this.slugifier.slugify(entity.getTitle()));
        }

        productStore.get().create(entity);

        return this.findProductBySlug(entity.getSlug());
    }

    public void updateProduct(Product entity) throws EntityDoesNotExistException, InvalidEntityException
    {
        this.productStore.get().update(entity);
    }

    public Product findProductBySlug(String slug)
    {
        return this.productStore.get().findBySlug(slug);
    }

    public List<Product> findAllProducts(int number, int offset)
    {
        return this.productStore.get().findAll(number, offset);
    }

    public List<Product> findUncategorizedProducts()
    {
        return this.productStore.get().findUncategorizedProducts();
    }

    @Override
    public void createCategory(Category entity) throws InvalidEntityException, EntityAlreadyExistsException
    {
        if (Strings.isNullOrEmpty(entity.getSlug())) {
            entity.setSlug(this.slugifier.slugify(entity.getTitle()));
        }
        this.categoryStore.get().create(entity);
    }

    @Override
    public void updateCategory(Category entity) throws EntityDoesNotExistException, InvalidEntityException
    {
        this.categoryStore.get().update(entity);
    }

    @Override
    public List<Category> findCategoriesForProduct(Product product)
    {
        return this.categoryStore.get().findAllForProduct(product);
    }

    @Override
    public List<Product> findProductsForCategory(Category category)
    {
        return this.productStore.get().findAllForCategory(category);
    }

    @Override
    public void addProductToCategory(String category, String product) throws InvalidOperation
    {
        Category c = this.findCategoryBySlug(category);
        Product p = this.findProductBySlug(product);
        if (p == null || c == null) {
            throw new InvalidOperation("Product or category does not exist");
        }
        List<Category> categories = this.categoryStore.get().findAllForProduct(p);
        if (categories.contains(c)) {
            // Already has it : nothing to do
            return;
        }
        this.categoryStore.get().addProduct(c, p);
    }

    @Override
    public void removeProductFromCategory(String category, String product) throws InvalidOperation
    {
        Category c = this.findCategoryBySlug(category);
        Product p = this.findProductBySlug(product);
        if (p == null || c == null) {
            throw new InvalidOperation("Product or category does not exist");
        }
        List<Category> categories = this.categoryStore.get().findAllForProduct(p);
        if (!categories.contains(c)) {
            // It does not contain it : nothing to do
            return;
        }
        this.categoryStore.get().removeProduct(c, p);
    }

    @Override
    public void moveProduct(String slugOfProductToMove, String slugOfProductToMoveBeforeOf)
            throws InvalidMoveOperation
    {
        this.moveProduct(slugOfProductToMove, slugOfProductToMoveBeforeOf, InsertPosition.BEFORE);
    }

    @Override
    public void moveProduct(String slugOfProductToMove, String slugOfProductToRelativeTo,
            InsertPosition position) throws InvalidMoveOperation
    {
        this.productStore.get().moveProduct(slugOfProductToMove, slugOfProductToRelativeTo,
                position.equals(InsertPosition.AFTER) ? HasOrderedCollections.RelativePosition.AFTER :
                        HasOrderedCollections.RelativePosition.BEFORE);
    }

    @Override
    public void moveCategory(String slugOfCategoryToMove, String slugOfCategoryToMoveBeforeOf)
            throws InvalidMoveOperation
    {
        this.moveCategory(slugOfCategoryToMove, slugOfCategoryToMoveBeforeOf, InsertPosition.BEFORE);
    }

    @Override
    public void moveCategory(String slugOfCategoryToMove, String slugOfCategoryToRelativeTo,
            InsertPosition position) throws InvalidMoveOperation
    {
        this.categoryStore.get().moveCategory(slugOfCategoryToMove, slugOfCategoryToRelativeTo,
                position.equals(InsertPosition.AFTER) ? HasOrderedCollections.RelativePosition.AFTER :
                        HasOrderedCollections.RelativePosition.BEFORE);
    }

    @Override
    public Category findCategoryBySlug(String slug)
    {
        return this.categoryStore.get().findBySlug(slug);
    }

    @Override
    public List<Category> findAllCategories(int number, int offset)
    {
        return this.categoryStore.get().findAll(number, offset);
    }

    @Override
    public List<EntityAndCount<Category>> findAllCategoriesWithProductCount()
    {
        return this.categoryStore.get().findAllWithProductCount();
    }

    @Override
    public void moveProductInCategory(Category category, String slugOfProductToMove, String relativeSlug)
            throws InvalidMoveOperation
    {
        this.moveProductInCategory(category, slugOfProductToMove, relativeSlug, InsertPosition.BEFORE);
    }

    @Override
    public void moveProductInCategory(Category category, String slugOfProductToMove, String relativeSlug,
            InsertPosition insertPosition) throws InvalidMoveOperation
    {
        throw new InvalidMoveOperation();
    }
}
