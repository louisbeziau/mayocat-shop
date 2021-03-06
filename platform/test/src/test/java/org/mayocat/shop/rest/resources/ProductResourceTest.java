package org.mayocat.shop.rest.resources;

import javax.ws.rs.core.MediaType;

import org.junit.Assert;
import org.junit.Test;
import org.mayocat.shop.rest.api.v1.resources.ProductResource;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

public class ProductResourceTest extends AbstractAuthenticatedResourceTest
{
    ProductResource productResource;

    @Override
    protected void setUpResources() throws Exception
    {
        super.setUpResources();

        this.productResource = this.componentManager.getInstance(Resource.class, "ProductResource");
        addResource(this.productResource);
    }

    @Test
    public void testGetInexistentProduct() throws Exception
    {
        ClientResponse cr = client().resource("/product/hoverboard")
                                    .header("Authorization", this.getBasicAuthenticationHeader())
                                    .get(ClientResponse.class);

        Assert.assertEquals(Status.NOT_FOUND, cr.getClientResponseStatus());
    }

    @Test
    public void testPutRequestRequireValidProduct() throws Exception
    {
        ClientResponse cr = client().resource("/product/")
                                    .type(MediaType.APPLICATION_JSON)
                                    .entity("{\"slug\":\"aiya\"}")
                                    .header("Authorization", this.getBasicAuthenticationHeader())
                                    .post(ClientResponse.class);

        Assert.assertEquals(Status.OK, cr.getClientResponseStatus());
    }

}
