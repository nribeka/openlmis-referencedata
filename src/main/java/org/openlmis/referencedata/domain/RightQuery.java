package org.openlmis.referencedata.domain;

import java.util.Objects;
import lombok.Getter;

@Getter
public class RightQuery {

  private Right right;
  private Program program;
  private Facility facility;
  private SupervisoryNode supervisoryNode;
  private Facility warehouse;

  /**
   * Constructor to create query if user has a right. This is for general admin and report rights.
   *
   * @param right the right to check
   */
  public RightQuery(Right right) {
    this.right = right;
  }

  /**
   * Constructor to create query if user has a right in a specified program at a specified facility.
   * This is for supervision rights.
   *
   * @param right    the right to check
   * @param program  the program to check
   * @param facility the facility to check
   */
  public RightQuery(Right right, Program program, Facility facility) {
    this.right = right;
    this.program = program;
    this.facility = facility;
  }

  /**
   * Constructor to create query if user has a right in a specified program at a specified
   * supervisory node. This is also for supervision rights.
   *
   * @param right           the right to check
   * @param program         the program to check
   * @param supervisoryNode the supervisory node to check
   */
  public RightQuery(Right right, Program program, SupervisoryNode supervisoryNode) {
    this.right = right;
    this.program = program;
    this.supervisoryNode = supervisoryNode;
  }

  /**
   * Constructor to create query if user has a right at a specified warehouse. This is for order
   * fulfillment rights.
   *
   * @param right     the right to check
   * @param warehouse the warehouse to check
   */
  public RightQuery(Right right, Facility warehouse) {
    this.right = right;
    this.warehouse = warehouse;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof RightQuery)) {
      return false;
    }
    RightQuery that = (RightQuery) other;
    return Objects.equals(right, that.right)
        && Objects.equals(program, that.program)
        && Objects.equals(facility, that.facility)
        && Objects.equals(supervisoryNode, that.supervisoryNode)
        && Objects.equals(warehouse, that.warehouse);
  }

  @Override
  public int hashCode() {
    return Objects.hash(right, program, facility, supervisoryNode, warehouse);
  }
}
