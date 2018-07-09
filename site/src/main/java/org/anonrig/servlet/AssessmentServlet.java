package org.anonrig.servlet;

import org.hippoecm.hst.site.HstServices;

import com.google.gson.*;
import javax.jcr.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssessmentServlet extends HttpServlet {
  private static Logger log = LoggerFactory.getLogger(AssessmentServlet.class);

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
    Repository repository = HstServices.getComponentManager().getComponent(Repository.class.getName());
    Session session = null;
    ArrayList<String> allNodes = null;

    try {
      session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
      Node rootNode = session.getRootNode();
      allNodes = findDescendants(rootNode.getNode("content"), new ArrayList<>());
    } catch (RepositoryException e) {
      log.error("RepositoryException; " +  e.toString());
      res.sendError(500, e.toString());
    } finally {
      String response = new Gson().toJson(allNodes);
      res.getWriter().print(response);
    }
  }

  private ArrayList<String> findDescendants(Node currentNode, ArrayList<String> allNodes) throws RepositoryException {
    NodeIterator iterator = currentNode.getNodes();

    while (iterator.hasNext()) {
      Node nextNode = iterator.nextNode();

      if (nextNode.isNode() && !nextNode.isNodeType("hippofacnav:facetnavigation")) {
        if (!nextNode.getNodes().hasNext()) {
          allNodes.add(nextNode.getName());
          log.info("Added descendant node; " + nextNode.getName());
        }
        findDescendants(nextNode, allNodes);
      }
    }

    return allNodes;
  }
}
