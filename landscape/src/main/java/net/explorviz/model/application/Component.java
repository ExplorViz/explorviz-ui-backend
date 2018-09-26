package net.explorviz.model.application;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import java.util.ArrayList;
import java.util.List;
import net.explorviz.model.helper.BaseEntity;

/**
 * Model representing a component (e.g., a package within a java application).
 */
@SuppressWarnings("serial")
@Type("component")
@JsonIgnoreProperties("belongingApplication")
public class Component extends BaseEntity {

  private String name;
  private String fullQualifiedName;

  @Relationship("children")
  private List<Component> children = new ArrayList<>();

  @Relationship("clazzes")
  private List<Clazz> clazzes = new ArrayList<>();

  @Relationship("parentComponent")
  private Component parentComponent;

  // @Relationship("belongingApplication")
  // Don't parse since cycle results in stackoverflow when accessing
  // latestLandscape
  private Application belongingApplication;

  public String getName() {
    return this.name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getFullQualifiedName() {
    return this.fullQualifiedName;
  }

  public void setFullQualifiedName(final String fullQualifiedName) {
    this.fullQualifiedName = fullQualifiedName;
  }

  public Component getParentComponent() {
    return this.parentComponent;
  }

  public void setParentComponent(final Component parentComponent) {
    this.parentComponent = parentComponent;
  }

  public Application getBelongingApplication() {
    return this.belongingApplication;
  }

  public void setBelongingApplication(final Application belongingApplication) {
    this.belongingApplication = belongingApplication;
  }

  public List<Component> getChildren() {
    return this.children;
  }

  public void setChildren(final List<Component> children) {
    this.children = children;
  }

  public List<Clazz> getClazzes() {
    return this.clazzes;
  }

  public void setClazzes(final List<Clazz> clazzes) {
    this.clazzes = clazzes;
  }
}
