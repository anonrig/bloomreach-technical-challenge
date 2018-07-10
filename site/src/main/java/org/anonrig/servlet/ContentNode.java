package org.anonrig.servlet;

import javax.jcr.*;
import java.util.HashMap;
import java.util.Map;

public class ContentNode {
  public String name;
  public Map<String, Object> properties;

  /**
   * @param name
   * @param properties
   * @throws RepositoryException
   */
  public ContentNode(String name, PropertyIterator properties) throws RepositoryException {
    this.name = name;
    this.properties = setProperties(properties);
  }

  /**
   * Set properties to a map.
   * @param properties
   * @return Map with key as string and object as value.
   * @throws RepositoryException
   */
  private Map<String, Object> setProperties(PropertyIterator properties) throws RepositoryException {
    Map<String, Object> result = new HashMap<>();
    while (properties.hasNext()) {
      try {
        Property property = properties.nextProperty();
        result.put(property.getName(), getValueFromProperty(property));
      } catch (Exception e) {
        System.out.println(e.getStackTrace());
      }

    }
    return result;
  }

  /**
   * Gets value from a property.
   * @param property
   * @return Object
   * @throws RepositoryException
   */
  private Object getValueFromProperty(Property property) throws RepositoryException {
    Object value;

    switch (property.getType()) {
      case PropertyType.BOOLEAN:
        value = property.getBoolean();
        break;
      case PropertyType.DATE:
        value = property.getDate().getTime();
        break;
      case PropertyType.STRING:
        value = property.getString();
        break;
      case PropertyType.LONG:
        value = property.getLong();
        break;
      case PropertyType.DECIMAL:
        value = property.getDecimal();
        break;
      default:
        value = null;
    }

    return value;
  }
}
