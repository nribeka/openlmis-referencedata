package org.openlmis.referencedata.domain;

public class RightQuery {

  private Right right;
  private Program program;
  private Facility facility;
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
   * This is for all supervision rights.
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

  public Right getRight() {
    return right;
  }

  public Program getProgram() {
    return program;
  }

  public Facility getFacility() {
    return facility;
  }

  public Facility getWarehouse() {
    return warehouse;
  }
}
