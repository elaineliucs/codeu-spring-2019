package com.google.codeu.servlets;
import com.google.appengine.api.blobstore.*;
import com.google.appengine.api.images.ImagesServiceFailureException;
import com.google.codeu.data.Datastore;
import com.google.codeu.data.Location;

import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.codeu.data.Message;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/location")
public class LocationServlet extends HttpServlet {

    private Datastore datastore;


    @Override
    public void init() {
        datastore = new Datastore();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");

        List<Location> locations = datastore.getLocations();
        Gson gson = new Gson();

        String json = gson.toJson(locations);

        response.getWriter().println(json);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {

        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
        List<BlobKey> blobKeys = blobs.get("image");

        /*
        if (blobKeys == null || blobKeys.isEmpty()) {
            response.sendRedirect("/");
        } else {
            response.sendRedirect("/serve?blob-key=" + blobKeys.get(0).getKeyString());
        }
        */

        // read form fields
        String name = request.getParameter("location-name");
        String description = request.getParameter("description");

        Location location = new Location(name, description, "", "");

        if(blobKeys != null && !blobKeys.isEmpty()) {
            BlobKey blobKey = blobKeys.get(0);
            ImagesService imagesService = ImagesServiceFactory.getImagesService();
            ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);
            try {
                String imageUrl = imagesService.getServingUrl(options);
                location.setImageUrl(imageUrl);
            } catch (ImagesServiceFailureException imageUrlc) {
                System.out.println("Error.");
            }

        }

        datastore.storeLocation(location);

    }

}