package org.mayocat.shop.api.v1.resources;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.mayocat.authorization.annotation.Authorized;
import org.mayocat.context.Context;
import org.mayocat.context.Execution;
import org.mayocat.accounts.model.Tenant;
import org.mayocat.accounts.model.User;
import org.mayocat.base.Resource;
import org.mayocat.accounts.AccountsService;
import org.mayocat.store.EntityDoesNotExistException;
import org.mayocat.store.InvalidEntityException;
import org.xwiki.component.annotation.Component;

@Component("/api/1.0/tenant/")
@Path("/api/1.0/tenant/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TenantResource implements Resource
{

    @Inject
    private Execution execution;

    @Inject
    private AccountsService accountsService;

    @GET
    @Authorized
    public UserAndTenant currentTenant()
    {
        UserAndTenant userAndTenant = new UserAndTenant();
        userAndTenant.setTenant(execution.getContext().getTenant());
        userAndTenant.setUser(execution.getContext().getUser());
        return userAndTenant;
    }

    @PUT
    @Authorized
    public Response updateTenant(Tenant updatedTenant)
    {
        try {
            Context context = execution.getContext();
            if (context.getTenant() == null) {
                // Should not happen
                return Response.status(404).build();
            } else {
                updatedTenant.setSlug(context.getTenant().getSlug());
                this.accountsService.updateTenant(updatedTenant);
            }

            return Response.ok().build();

        } catch (InvalidEntityException e) {
            throw new com.yammer.dropwizard.validation.InvalidEntityException(e.getMessage(), e.getErrors());
        } catch (EntityDoesNotExistException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Tenant not found\n").type(MediaType.TEXT_PLAIN_TYPE).build();
        }
    }

    @POST
    public Response createTenant(UserAndTenant userAndTenant)
    {
        // TODO
        return Response.ok().build();
    }

    private class UserAndTenant
    {
        private User user;

        private Tenant tenant;

        public Tenant getTenant()
        {
            return tenant;
        }

        public void setTenant(Tenant tenant)
        {
            this.tenant = tenant;
        }

        public User getUser()
        {
            return user;
        }

        public void setUser(User user)
        {
            this.user = user;
        }
    }

}
