package wdsr.exercise3.client;


import org.codehaus.jackson.map.ObjectMapper;
import wdsr.exercise3.model.Product;
import wdsr.exercise3.model.ProductType;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ProductService extends RestClientBase {

    private static final String PRODUCT_SERVICE_URL = "/products";
    private static final String ID_TEMPLATE = "/{id}";
    private static final String ID_RESOLVE_TEMPLATE = "id";


    protected ProductService(final String serverHost, final int serverPort, final Client client) {
        super(serverHost, serverPort, client);
    }

    /**
     * Looks up all products of given types known to the server.
     *
     * @param types Set of types to be looked up
     * @return A list of found products - possibly empty, never null.
     */
    public List<Product> retrieveProducts(Set<ProductType> types) {
        List<Product> products = new ArrayList<>();
        Response response = baseTarget
                .path(PRODUCT_SERVICE_URL)
                .request(MediaType.APPLICATION_JSON)
                .get(Response.class);

        List<Product> allProducts = response.readEntity(new GenericType<List<Product>>() {
        });
        for (Product product : allProducts) {
            for (ProductType current : types) {
                if (current.equals(product.getType()))
                    products.add(product);
            }
        }

        return products;
    }

    /**
     * Looks up all products known to the server.
     *
     * @return A list of all products - possibly empty, never null.
     */
    public List<Product> retrieveAllProducts() {
        Response response = baseTarget
                .path(PRODUCT_SERVICE_URL)
                .request(MediaType.APPLICATION_JSON)
                .get(Response.class);

        return response.readEntity(new GenericType<List<Product>>() {
        });
    }

    /**
     * Looks up the product for given ID on the server.
     *
     * @param id Product ID assigned by the server
     * @return Product if found
     * @throws NotFoundException if no product found for the given ID.
     */
    public Product retrieveProduct(int id) {
        Response response = baseTarget
                .path(PRODUCT_SERVICE_URL + ID_TEMPLATE)
                .resolveTemplate(ID_RESOLVE_TEMPLATE, id)
                .request(MediaType.APPLICATION_JSON)
                .get(Response.class);

        Product product = null;
        String json = response.readEntity(String.class);
        try {
            product = new ObjectMapper().readValue(json, Product.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (product != null) {
            return product;
        } else {
            throw new NotFoundException();
        }
    }

    /**
     * Creates a new product on the server.
     *
     * @param product Product to be created. Must have null ID field.
     * @return ID of the new product.
     * @throws WebApplicationException if request to the server failed
     */
    public int storeNewProduct(Product product) {
        if (product.getId() != null) {
            throw new WebApplicationException("Supplied Product already has an ID");
        }
        Response response = baseTarget
                .path(PRODUCT_SERVICE_URL)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(product, MediaType.APPLICATION_JSON));
        response.close();
        return Integer.parseInt(Paths.get(response.getLocation().getPath()).getFileName().toString());
    }

    /**
     * Updates the given product.
     *
     * @param product Product with updated values. Its ID must identify an existing resource.
     * @throws NotFoundException if no product found for the given ID.
     */
    public void updateProduct(Product product) {
        Response response = baseTarget
                .path(PRODUCT_SERVICE_URL + ID_TEMPLATE)
                .resolveTemplate(ID_RESOLVE_TEMPLATE, product.getId())
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.entity(product, MediaType.APPLICATION_JSON));
        response.close();
        if (response.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
            throw new NotFoundException("There is no product with id " + product.getId());
        }
    }


    /**
     * Deletes the given product.
     *
     * @param product Product to be deleted. Its ID must identify an existing resource.
     * @throws NotFoundException if no product found for the given ID.
     */
    public void deleteProduct(Product product) {
    	

        Response response = baseTarget
                .path(PRODUCT_SERVICE_URL + ID_TEMPLATE)
                .resolveTemplate(ID_RESOLVE_TEMPLATE, product.getId())
                .request(MediaType.APPLICATION_JSON)
                .delete();
        response.close();
        if (response.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
            throw new NotFoundException();
        }
    }
}
