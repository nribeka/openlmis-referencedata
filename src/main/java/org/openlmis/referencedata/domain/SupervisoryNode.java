package org.openlmis.referencedata.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "supervisory_nodes", schema = "referencedata")
@NoArgsConstructor
public class SupervisoryNode extends BaseEntity {

  @Column(nullable = false, unique = true, columnDefinition = "text")
  @Getter
  @Setter
  private String code;

  @Column(columnDefinition = "text")
  @Getter
  @Setter
  private String name;

  @Column(columnDefinition = "text")
  @Getter
  @Setter
  private String description;

  @ManyToOne
  @JoinColumn(nullable = false, name = "facilityid")
  @Getter
  @Setter
  private Facility facility;

  @ManyToOne
  @JoinColumn(name = "parentid")
  @Getter
  private SupervisoryNode parentNode;

  @OneToMany(mappedBy = "parentNode")
  @Getter
  private Set<SupervisoryNode> childNodes;

  @OneToOne(mappedBy = "supervisoryNode")
  @Getter
  @Setter
  private RequisitionGroup requisitionGroup;

  private SupervisoryNode(String code, Facility facility) {
    this.code = code;
    this.facility = facility;
    this.childNodes = new HashSet<>();
  }

  /**
   * Create a new supervisory node.
   *
   * @param facility facility associated with this supervisory node
   * @return a new SupervisoryNode
   */
  public static SupervisoryNode newSupervisoryNode(String code, Facility facility) {
    return new SupervisoryNode(code, facility);
  }

  /**
   * Static factory method for constructing a new supervisory node using an importer (DTO).
   *
   * @param importer the supervisory node importer (DTO)
   */
  public static SupervisoryNode newSupervisoryNode(Importer importer) {
    SupervisoryNode newSupervisoryNode = new SupervisoryNode(importer.getCode(),
        importer.getFacility());
    newSupervisoryNode.id = importer.getId();
    newSupervisoryNode.name = importer.getName();
    newSupervisoryNode.description = importer.getDescription();

    if (importer.getParentNode() != null) {
      newSupervisoryNode.parentNode = SupervisoryNode.newSupervisoryNode(importer.getParentNode());
    }

    if (importer.getRequisitionGroup() != null) {
      newSupervisoryNode.requisitionGroup =
          RequisitionGroup.newRequisitionGroup(importer.getRequisitionGroup());
    }

    if (importer.getChildNodes() != null) {
      Set<SupervisoryNode> childNodes = new HashSet<>();

      for (Importer childNodeImporter : importer.getChildNodes()) {
        childNodes.add(SupervisoryNode.newSupervisoryNode(childNodeImporter));
      }

      newSupervisoryNode.childNodes = childNodes;
    }

    return newSupervisoryNode;
  }

  /**
   * Assign this node's parent supervisory node. Also add this node to the parent's set of child
   * nodes.
   *
   * @param parentNode parent supervisory node to assign.
   */
  public void assignParentNode(SupervisoryNode parentNode) {
    this.parentNode = parentNode;
    parentNode.childNodes.add(this);
  }

  /**
   * Get all facilities being supervised by this supervisory node. Note, this does not get the
   * facility attached to this supervisory node. "All supervised facilities" means all facilities
   * supervised by this node and all recursive child nodes.
   *
   * @return all supervised facilities
   */
  public Set<Facility> getAllSupervisedFacilities() {
    Set<Facility> supervisedFacilities = new HashSet<>();

    if (requisitionGroup != null && requisitionGroup.getMemberFacilities() != null) {
      supervisedFacilities.addAll(requisitionGroup.getMemberFacilities());
    }

    if (childNodes != null) {
      for (SupervisoryNode childNode : childNodes) {
        supervisedFacilities.addAll(childNode.getAllSupervisedFacilities());
      }
    }

    return supervisedFacilities;
  }

  /**
   * Copy values of attributes into new or updated SupervisoryNode.
   *
   * @param supervisoryNode SupervisoryNode with new values.
   */
  public void updateFrom(SupervisoryNode supervisoryNode) {
    this.code = supervisoryNode.getCode();
    this.name = supervisoryNode.getName();
    this.description = supervisoryNode.getDescription();
    this.facility = supervisoryNode.getFacility();
    this.parentNode = supervisoryNode.getParentNode();
    this.childNodes = supervisoryNode.getChildNodes();
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(Exporter exporter) {
    exporter.setId(id);
    exporter.setCode(code);
    exporter.setName(name);
    exporter.setDescription(description);
    exporter.setFacility(facility);
    exporter.setParentNode(parentNode);
    exporter.setChildNodes(childNodes);
    exporter.setRequisitionGroup(requisitionGroup);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SupervisoryNode)) {
      return false;
    }
    SupervisoryNode that = (SupervisoryNode) obj;
    return Objects.equals(code, that.code);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code);
  }

  public interface Exporter {
    void setId(UUID id);

    void setCode(String code);

    void setName(String name);

    void setDescription(String description);

    void setFacility(Facility facility);

    void setParentNode(SupervisoryNode parentNode);

    void setChildNodes(Set<SupervisoryNode> childNodes);

    void setRequisitionGroup(RequisitionGroup requisitionGroup);
  }

  public interface Importer {
    UUID getId();

    String getCode();

    String getName();

    String getDescription();

    Facility getFacility();

    SupervisoryNode.Importer getParentNode();

    Set<SupervisoryNode.Importer> getChildNodes();

    RequisitionGroup.Importer getRequisitionGroup();
  }
}
