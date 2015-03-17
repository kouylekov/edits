package org.edits.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import lombok.Getter;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.LogManager;
import org.edits.engines.EvaluationResult;
import org.edits.etaf.AnnotatedEntailmentPair;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.api.core.DefaultResourceConfig;

@Path("system")
public class RestService {

	@Getter
	private static EngineManager manager = null;

	public static void init() {
		if (manager == null) {
			try {
				manager = new EngineManager("/home/milen/export/edits-models/");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("restriction")
	public static void main(String[] args) throws IllegalArgumentException, IOException {

		System.setProperty("log4j.defaultInitOverride", "true");
		LogManager.resetConfiguration();

		DefaultResourceConfig resourceConfig = new DefaultResourceConfig(RestService.class);
		HttpServerFactory.create("http://localhost:8095/", resourceConfig).start();

		// test();

	}

	public static void test() throws ClientProtocolException, IOException {
		HttpClient client = HttpClients.createDefault();
		HttpGet request = new HttpGet("http://localhost:8095/system/evaluate?engine=rte7&t=b&h=b");
		HttpResponse response = client.execute(request);
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line = "";
		while ((line = rd.readLine()) != null) {
			System.out.println(line);
		}
		rd.close();
		request = new HttpGet("http://localhost:8095/system/evaluate?engine=rte7&t=a&h=b");
		response = client.execute(request);
		rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		line = "";
		while ((line = rd.readLine()) != null) {
			System.out.println(line);
		}
		rd.close();
		System.exit(0);
	}

	@Path("evaluate")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public Response evaluate(@DefaultValue(EngineManager.DEFAULT_ENTAILMENT_ENGINE) @QueryParam("engine") String id,
			@QueryParam("t") String t, @QueryParam("h") String h) {
		try {
			init();
			EngineInstance e = manager.get(id);
			AnnotatedEntailmentPair p = AnnotatedEntailmentPair.create(t, h, e.annotator());
			System.out.println(p);
			EvaluationResult r = e.getModel().getEngine().evaluate(p);
			Response response = Response.ok(r, MediaType.APPLICATION_JSON).build();
			return response;
		} catch (Exception ex) {
			Logger.getLogger(RestService.class.getName()).log(Level.SEVERE, "Error generating response", ex);
			return Response.ok(ex.getMessage()).build();
		}
	}

	@Path("statistics")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public Response statistics(@QueryParam("engine") String id) {
		init();
		return Response.ok(manager.status(id)).build();
	}

	@Path("status")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public Response status(@QueryParam("engine") String id) {
		init();
		return Response.ok(manager.statistics(id)).build();
	}
}