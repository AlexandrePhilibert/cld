package ch.heigvd.cld.lab;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

@WebServlet(name = "DataStoreWrite", value = "/datastorewrite")
public class DataStoreWrite extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain");
        PrintWriter pw = resp.getWriter();

        Map<String, String[]> parameterMap = req.getParameterMap();
        if (!parameterMap.containsKey("_kind")) {
            resp.setStatus(400);
            pw.write("The kind parameter is required but was not found.");
            return;
        }

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Entity entity;
        if (parameterMap.containsKey("_key")) {
            entity = new Entity(req.getParameter("_kind"), req.getParameter("_key"));
        } else {
            entity = new Entity(req.getParameter("_kind"));
        }


        Enumeration<String> parameters = req.getParameterNames();

        Stream.generate(() -> null)
                .takeWhile(x -> parameters.hasMoreElements())
                .map(n -> parameters.nextElement())
                .filter(Predicate.not(List.of("_key", "_kind")::contains))
                .forEach(key -> entity.setProperty(key, req.getParameter(key)));

        datastore.put(entity);


        pw.write("Entity was written to datastore!");
    }
}
