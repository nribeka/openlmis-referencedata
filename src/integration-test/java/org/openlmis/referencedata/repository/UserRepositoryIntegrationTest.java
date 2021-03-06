/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.referencedata.repository;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.openlmis.referencedata.domain.RightType.GENERAL_ADMIN;
import static org.openlmis.referencedata.domain.RightType.ORDER_FULFILLMENT;
import static org.openlmis.referencedata.domain.RightType.REPORTS;
import static org.openlmis.referencedata.domain.RightType.SUPERVISION;
import static org.powermock.api.mockito.PowerMockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.FulfillmentRoleAssignment;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightType;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.domain.RoleAssignment;
import org.openlmis.referencedata.domain.SupervisionRoleAssignment;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.testbuilder.DirectRoleAssignmentDataBuilder;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.testbuilder.FacilityTypeDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicLevelDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicZoneDataBuilder;
import org.openlmis.referencedata.testbuilder.RightDataBuilder;
import org.openlmis.referencedata.testbuilder.RoleDataBuilder;
import org.openlmis.referencedata.testbuilder.SupervisoryNodeDataBuilder;
import org.openlmis.referencedata.testbuilder.UserDataBuilder;
import org.openlmis.referencedata.util.UserSearchParamsDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.UnusedPrivateFiled"})
public class UserRepositoryIntegrationTest extends BaseCrudRepositoryIntegrationTest<User> {

  private static final String EXTRA_DATA_KEY = "color";
  private static final String EXTRA_DATA_VALUE = "orange";
  private static final int TOTAL_USERS = 5;

  @Autowired
  private UserRepository repository;

  @Autowired
  private GeographicLevelRepository geographicLevelRepository;

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  @Autowired
  private FacilityTypeRepository facilityTypeRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private RightRepository rightRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private SupervisoryNodeRepository supervisoryNodeRepository;

  @Autowired
  private EntityManager entityManager;

  @Mock
  private Pageable pageable;

  private List<User> users;

  private ObjectMapper mapper = new ObjectMapper();

  UserRepository getRepository() {
    return this.repository;
  }

  User generateInstance() {
    int instanceNumber = this.getNextInstanceNumber();
    return new UserDataBuilder()
        .withHomeFacilityId(generateFacility(instanceNumber).getId())
        .buildAsNew();
  }

  @Before
  public void setUp() {

    users = new ArrayList<>();

    for (int usersCount = 0; usersCount < TOTAL_USERS - 1; usersCount++) {
      users.add(repository.save(generateInstance()));
    }

    when(pageable.getPageNumber()).thenReturn(0);
    when(pageable.getPageSize()).thenReturn(10);
  }

  @Test
  public void testSearchUsersByAllParameters() {
    User user = cloneUser(users.get(0));
    UserSearchParams searchParams = new UserSearchParamsDataBuilder()
        .asEmpty()
        .withUsername(user.getUsername())
        .withFirstName(user.getFirstName())
        .withLastName(user.getLastName())
        .withHomeFacilityId(user.getHomeFacilityId())
        .withActive(user.isActive())
        .withId(Sets.newHashSet(user.getId().toString()))
        .build();
    Page<User> receivedUsers = repository.searchUsers(searchParams, null, pageable);

    assertEquals(1, receivedUsers.getContent().size());
    assertEquals(1, receivedUsers.getTotalElements());

    User receivedUser = receivedUsers.getContent().get(0);
    assertEquals(user.getUsername(), receivedUser.getUsername());
    assertEquals(user.getFirstName(), receivedUser.getFirstName());
    assertEquals(user.getLastName(), receivedUser.getLastName());
    assertEquals(user.getHomeFacilityId(), receivedUser.getHomeFacilityId());
    assertEquals(user.isActive(), receivedUser.isActive());
  }

  @Test
  public void searchUsersWithAllParametersNullShouldReturnAllUsers() {
    Page<User> receivedUsers = repository
        .searchUsers(new UserSearchParamsDataBuilder().asEmpty().build(), null, pageable);
    assertEquals(TOTAL_USERS, receivedUsers.getContent().size());
  }

  @Test
  public void testSearchUsersByFirstNameAndLastNameAndHomeFacility() {
    User user = cloneUser(users.get(0));
    UserSearchParams searchParams = new UserSearchParamsDataBuilder()
        .asEmpty()
        .withFirstName(user.getFirstName())
        .withLastName(user.getLastName())
        .withHomeFacilityId(user.getHomeFacilityId())
        .build();
    Page<User> receivedUsers = repository.searchUsers(searchParams, null, pageable);

    assertEquals(2, receivedUsers.getContent().size());
    for (User receivedUser : receivedUsers) {
      assertEquals(
          user.getFirstName(),
          receivedUser.getFirstName());
      assertEquals(
          user.getLastName(),
          receivedUser.getLastName());
      assertEquals(
          user.getHomeFacilityId(),
          receivedUser.getHomeFacilityId());
    }
  }

  @Test
  public void testSearchUsersShouldReturnAllUsersListIfEmptyListIsPassed() {
    Page<User> receivedUsers = repository
        .searchUsers(new UserSearchParamsDataBuilder().asEmpty().build(), Collections.emptyList(),
            pageable);

    assertEquals(TOTAL_USERS, receivedUsers.getContent().size());
  }

  @Test
  public void testSearchSortByUsername() {
    Sort sort = Sort.by(new Sort.Order(Sort.Direction.ASC, "username"));
    when(pageable.getSort()).thenReturn(sort);

    Page<User> receivedUsers = repository
        .searchUsers(new UserSearchParamsDataBuilder().asEmpty().build(), null, pageable);

    for (int i = 1; i < receivedUsers.getContent().size(); i++) {
      assertTrue(receivedUsers.getContent().get(i).getUsername()
          .compareTo(receivedUsers.getContent().get(i - 1).getUsername()) > 0);
    }
  }

  @Test
  public void shouldFindAllByIds() {
    // given users I want
    User user1 = repository.save(generateInstance());
    User user2 = repository.save(generateInstance());

    // given a user I don't want
    repository.save(generateInstance());

    // when
    Set<String> ids = Sets.newHashSet(user1.getId().toString(), user2.getId().toString());
    UserSearchParams searchParams = new UserSearchParamsDataBuilder()
        .asEmpty()
        .withId(ids)
        .build();
    Page<User> found = repository.searchUsers(searchParams, null, pageable);

    // then
    assertEquals(2, found.getContent().size());
    assertThat(found.getContent(), hasItems(user1, user2));
  }

  @Test
  public void shouldFindByIdsAndHomeFacilityId() {
    // given users I want
    User user1 = repository.save(generateInstance());

    // given a user I don't want
    User user2 = repository.save(generateInstance());
    User user3 = repository.save(generateInstance());

    // when
    UserSearchParams searchParams = new UserSearchParamsDataBuilder()
        .asEmpty()
        .withId(Sets.newHashSet(user1.getId().toString(), user2.getId().toString()))
        .withHomeFacilityId(user1.getHomeFacilityId())
        .build();
    Page<User> found = repository.searchUsers(searchParams, ImmutableList.of(user3), pageable);

    // then
    assertEquals(1, found.getContent().size());
    assertThat(found.getContent(), hasItem(user1));
  }

  @Test
  public void findByExtraDataShouldFindDataWhenParametersMatch() throws JsonProcessingException {
    //given
    Map<String, Object> extraData = Collections.singletonMap(EXTRA_DATA_KEY, EXTRA_DATA_VALUE);
    String extraDataJson = mapper.writeValueAsString(extraData);
    User expectedUser = repository.findOneByUsernameIgnoreCase(users.get(0).getUsername());
    expectedUser.setExtraData(extraData);
    repository.save(expectedUser);

    //when
    List<User> extraDataUsers = repository.findByExtraData(extraDataJson);

    //then
    assertEquals(1, extraDataUsers.size());

    User user = extraDataUsers.get(0);
    assertEquals(expectedUser.getUsername(), user.getUsername());
    assertEquals(expectedUser.getFirstName(), user.getFirstName());
    assertEquals(expectedUser.getLastName(), user.getLastName());
    assertEquals(expectedUser.getTimezone(), user.getTimezone());
    assertEquals(expectedUser.getHomeFacilityId(), user.getHomeFacilityId());
    assertEquals(expectedUser.isActive(), user.isActive());
    assertEquals(expectedUser.getExtraData(), user.getExtraData());
  }

  @Test
  public void findByExtraDataShouldNotFindDataWhenParametersDoNotMatch() {
    //given
    String otherExtraDataJson = "{\"" + EXTRA_DATA_KEY + "\":\"blue\"}";

    //when
    List<User> extraDataUsers = repository.findByExtraData(otherExtraDataJson);

    //then
    assertEquals(0, extraDataUsers.size());
  }

  @Test
  public void findSupervisingUsersByShouldOnlyFindMatchingUsers() {
    //given
    Right right = saveNewRight("right", SUPERVISION);
    Role role = saveNewRole("role", right);
    Program program = saveNewProgram("P1");
    SupervisoryNode supervisoryNode = saveNewSupervisoryNode("SN1", generateFacility(10));

    User supervisingUser = repository.findOneByUsernameIgnoreCase(users.get(0).getUsername());
    supervisingUser = assignRoleToUser(supervisingUser,
        new SupervisionRoleAssignment(role, supervisingUser, program, supervisoryNode));

    //when
    Set<User> supervisingUsers = repository.findUsersBySupervisionRight(right.getId(),
        supervisoryNode.getId(), program.getId());

    //then
    assertEquals(1, supervisingUsers.size());
    assertEquals(supervisingUser, supervisingUsers.iterator().next());
  }

  @Test
  public void findSupervisingUsersByShouldOnlyFindMatchingUsersWithoutSupervisoryNode() {
    //given
    Right right = saveNewRight("right", SUPERVISION);
    Role role = saveNewRole("role", right);
    Program program = saveNewProgram("P1");

    User supervisingUser = repository.findOneByUsernameIgnoreCase(users.get(0).getUsername());
    supervisingUser = assignRoleToUser(supervisingUser,
        new SupervisionRoleAssignment(role, supervisingUser, program));

    //when
    Set<User> supervisingUsers = repository.findUsersBySupervisionRight(right.getId(),
        program.getId());

    //then
    assertEquals(1, supervisingUsers.size());
    assertEquals(supervisingUser, supervisingUsers.iterator().next());
  }

  @Test
  public void shouldFindUsersByFulfillmentRights() {
    //given
    Right supervisionRight = saveNewRight("supervisionRight", SUPERVISION);
    Role supervisionRole = saveNewRole("supervisionRole", supervisionRight);
    Program program = saveNewProgram("P1");
    SupervisoryNode supervisoryNode = saveNewSupervisoryNode("SN1", generateFacility(10));

    User supervisingUser = repository.findOneByUsernameIgnoreCase(users.get(0).getUsername());
    assignRoleToUser(supervisingUser, new SupervisionRoleAssignment(
        supervisionRole, supervisingUser, program, supervisoryNode));

    User supervisingUser2 = repository.findOneByUsernameIgnoreCase(users.get(1).getUsername());
    assignRoleToUser(supervisingUser2, new SupervisionRoleAssignment(
        supervisionRole, supervisingUser2, program, supervisoryNode));

    Right fulfillmentRight = saveNewRight("fulfillmentRight", ORDER_FULFILLMENT);
    Role fulfillmentRole = saveNewRole("fulfillmentRole", fulfillmentRight);
    Facility warehouse = generateFacility("warehouse");

    User warehouseClerk = repository.findOneByUsernameIgnoreCase(users.get(2).getUsername());
    warehouseClerk = assignRoleToUser(warehouseClerk,
        new FulfillmentRoleAssignment(fulfillmentRole, warehouseClerk, warehouse));

    User warehouseClerk2 = repository.findOneByUsernameIgnoreCase(users.get(3).getUsername());
    warehouseClerk2 = assignRoleToUser(warehouseClerk2,
        new FulfillmentRoleAssignment(fulfillmentRole, warehouseClerk2, warehouse));

    // when
    Set<User> found = repository.findUsersByFulfillmentRight(fulfillmentRight, warehouse);

    // then
    assertThat(found, hasSize(2));
    assertThat(found, hasItems(warehouseClerk, warehouseClerk2));
  }

  @Test
  public void shouldFindUsersByDirectRole() {
    //given
    Right reportRight = saveNewRight("reportRight", REPORTS);
    Role reportRole = saveNewRole("reportRole", reportRight);
    Right adminRight = saveNewRight("adminRight", GENERAL_ADMIN);
    Role adminRole = saveNewRole("adminRole", adminRight);

    User user1 = repository.findOneByUsernameIgnoreCase(users.get(0).getUsername());
    User user2 = repository.findOneByUsernameIgnoreCase(users.get(1).getUsername());

    user1.assignRoles(
        new DirectRoleAssignmentDataBuilder()
            .withRole(reportRole)
            .withUser(user1)
            .buildAsNew(),
        new DirectRoleAssignmentDataBuilder()
            .withRole(adminRole)
            .withUser(user1)
            .buildAsNew());
    user2.assignRoles(
        new DirectRoleAssignmentDataBuilder()
            .withRole(reportRole)
            .withUser(user2)
            .buildAsNew());

    // when
    Set<User> reportUsers = repository.findUsersByDirectRight(reportRight);
    Set<User> adminUsers = repository.findUsersByDirectRight(adminRight);

    // then
    assertThat(reportUsers, hasSize(2));
    assertThat(reportUsers, hasItems(user1, user2));
    assertThat(adminUsers, hasSize(1));
    assertThat(adminUsers, hasItem(user1));
  }

  @Test
  public void shouldFindUserByUsernameIgnoringCasing() {
    User user = generateInstance();
    repository.save(user);

    String searchTerm = user.getUsername().toUpperCase();
    User found = repository.findOneByUsernameIgnoreCase(searchTerm);

    assertNotNull(found);
    assertEquals(user.getUsername(), found.getUsername());
  }


  @Test(expected = PersistenceException.class)
  public void shouldThrowExceptionOnCreatingSameUsernameWithDifferentCasing() {
    User user = generateInstance();
    repository.save(user);

    User newUser = generateInstance();
    newUser.setUsername(user.getUsername().toUpperCase());
    repository.save(newUser);

    entityManager.flush();
  }

  private User cloneUser(User user) {
    int instanceNumber = this.getNextInstanceNumber();
    User clonedUser = new UserDataBuilder()
        .withUsername(user.getUsername() + instanceNumber)
        .withFirstName(user.getFirstName())
        .withLastName(user.getLastName())
        .withHomeFacilityId(user.getHomeFacilityId())
        .withActive(user.isActive())
        .buildAsNew();

    repository.save(clonedUser);
    return clonedUser;
  }

  private Facility generateFacility(int instanceNumber) {
    return generateFacility("FacilityCode" + instanceNumber);
  }

  private Facility generateFacility(String type) {
    GeographicLevel geographicLevel = generateGeographicLevel();
    GeographicZone geographicZone = generateGeographicZone(geographicLevel);
    FacilityType facilityType = generateFacilityType(type);
    Facility facility = new FacilityDataBuilder()
        .withType(facilityType)
        .withGeographicZone(geographicZone)
        .withoutOperator()
        .buildAsNew();
    facilityRepository.save(facility);
    return facility;
  }

  private GeographicLevel generateGeographicLevel() {
    GeographicLevel geographicLevel = new GeographicLevelDataBuilder()
        .withLevelNumber(1)
        .buildAsNew();
    geographicLevelRepository.save(geographicLevel);
    return geographicLevel;
  }

  private GeographicZone generateGeographicZone(GeographicLevel geographicLevel) {
    GeographicZone geographicZone = new GeographicZoneDataBuilder()
        .withLevel(geographicLevel)
        .buildAsNew();
    geographicZoneRepository.save(geographicZone);
    return geographicZone;
  }

  private FacilityType generateFacilityType(String type) {
    if ("warehouse".equals(type)) {
      return facilityTypeRepository.findOneByCode(type);
    }
    FacilityType facilityType = new FacilityTypeDataBuilder().withCode(type).buildAsNew();
    facilityTypeRepository.save(facilityType);
    return facilityType;
  }

  private Right saveNewRight(String name, RightType type) {
    Right right =  new RightDataBuilder().withType(type).withName(name).buildAsNew();
    return rightRepository.save(right);
  }

  private Role saveNewRole(String name, Right right) {
    Role role = new RoleDataBuilder().withRights(right).withName(name).buildAsNew();
    return roleRepository.save(role);
  }

  private Program saveNewProgram(String code) {
    Program program = new Program(code);
    return programRepository.save(program);
  }

  private SupervisoryNode saveNewSupervisoryNode(String code, Facility facility) {
    SupervisoryNode supervisoryNode = new SupervisoryNodeDataBuilder()
        .withCode(code)
        .withFacility(facility)
        .withoutId()
        .build();
    return supervisoryNodeRepository.save(supervisoryNode);
  }

  private User assignRoleToUser(User user, RoleAssignment assignment) {
    user.assignRoles(assignment);
    return repository.save(user);
  }
}
