package org.anonrig.servlet;


import com.google.common.base.Strings;
import org.apache.commons.lang.StringEscapeUtils;
import org.hippoecm.hst.site.HstServices;

import com.google.gson.*;
import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssessmentServlet extends HttpServlet {
  private static Logger log = LoggerFactory.getLogger(AssessmentServlet.class);

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
    Repository repository = HstServices.getComponentManager().getComponent(Repository.class.getName());
    String queryString = req.getQueryString();
    String searchKeyword = parseQueryString(queryString).get("search");

    try {
      ArrayList<ContentNode> allNodes = null;
      Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
      Workspace workspace = session.getWorkspace();

      // Search operation if keyword "search" and value is in query string.
      if (!Strings.isNullOrEmpty(searchKeyword)) {
        String escapedKeyword = StringEscapeUtils.escapeJava(searchKeyword);
        log.info("Log operation working with keyword; " + escapedKeyword);
        Query searchQuery = workspace.getQueryManager().createQuery("//*[jcr:contains(.,'" + escapedKeyword + "')]", Query.XPATH);
        allNodes = listNodes(searchQuery.execute().getNodes());
      } else {
        // Get all descendants.
        Node rootNode = session.getRootNode();
        allNodes = findDescendants(rootNode.getNode("content"), new ArrayList<>());
      }

      String response = new Gson().toJson(allNodes);
      res.getWriter().print(response);
      session.logout();
    } catch (Exception e) {
      log.error("Exception; " +  e.toString());
      res.sendError(500, e.toString());
    }
  }

  /**
   * Lists all nodes.
   * @param iterator
   * @return HAshMap with name and identifier.
   * @throws RepositoryException
   */
  private ArrayList<ContentNode> listNodes(NodeIterator iterator) throws RepositoryException {
    ArrayList<ContentNode> response = new ArrayList<>();

    while (iterator.hasNext()) {
      Node nextNode = iterator.nextNode();
      ContentNode node = new ContentNode(nextNode.getName(), nextNode.getProperties());
      response.add(node);
    }

    return response;
  }

  /**
   * Find descendants of an existing node
   * @param currentNode Starting node
   * @param allNodes Initial storage for which you want to store all descendant nodes.
   * @return Descendant nodes.
   * @throws RepositoryException
   */
  private ArrayList<ContentNode> findDescendants(Node currentNode, ArrayList<ContentNode> allNodes) throws RepositoryException {
    NodeIterator iterator = currentNode.getNodes();

    while (iterator.hasNext()) {
      Node nextNode = iterator.nextNode();

      if (nextNode.isNode() && !nextNode.isNodeType("hippofacnav:facetnavigation")) {
        if (!nextNode.getNodes().hasNext()) {
          allNodes.add(new ContentNode(nextNode.getName(), nextNode.getProperties()));
          log.info("Added descendant node; " + nextNode.getName());
        }
        findDescendants(nextNode, allNodes);
      }
    }

    return allNodes;
  }

  /**
   * Parses a query string and returns a map.
   * @param queryString
   * @return Returns key value pair of query string.
   * @throws UnsupportedEncodingException
   */
  private static Map<String, String> parseQueryString(String queryString) throws UnsupportedEncodingException {
    if (queryString == null || queryString.isEmpty()) {
      return Collections.EMPTY_MAP;
    }

    String[] queryParams = queryString.split("&");
    Map<String, String> map = new HashMap<>();

    for (String queryParam : queryParams) {
      int index = queryParam.indexOf('=');
      String key = queryParam.substring(0, index);
      key = URLDecoder.decode(key, "UTF-8");

      String value = null;

      if (index > 0) {
        value = queryParam.substring(index + 1);
        value = URLDecoder.decode(value, "UTF-8");
      }

      map.put(key, value);
    }

    return map;
  }
}
