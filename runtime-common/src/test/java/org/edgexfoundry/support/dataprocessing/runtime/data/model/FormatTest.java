package org.edgexfoundry.support.dataprocessing.runtime.data.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;

public class FormatTest {

  @Test
  public void testSimpleFormat() throws Exception {
    Person p = new Person("Joey", 3);
    Assert.assertNotNull(p.toString());

    String json = p.toString();

    Person pCopied = Format.create(json, Person.class);
    Assert.assertEquals(p, pCopied);
  }

  @Test
  public void testInvalidObjectMapper() throws Exception {
    try {
      Format.create("invalid data", Person.class);
      Assert.fail("Should not reach here");
    } catch (Exception e) {
      // success
    }

    Person p = new Person("Joey", 3);
    // Mock mapper
    ObjectMapper objectMapper = Mockito.spy(new ObjectMapper());
    Mockito.when(objectMapper.writeValueAsString(Mockito.any()))
        .thenThrow(new JsonProcessingException("JsonProcessingException mocked!") {
        });
    Whitebox.setInternalState(p, "mapper", objectMapper);
    try {
      p.toString();
      Assert.fail("Should not reach here.");
    } catch (RuntimeException e) {
      // Success
    }
  }

  private static class Person extends Format {

    String name;
    int age;

    public Person() {

    }

    public Person(String name, int age) {
      this.name = name;
      this.age = age;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public int getAge() {
      return age;
    }

    public void setAge(int age) {
      this.age = age;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof Person)) {
        return false;
      }
      Person other = (Person) obj;
      return this.name.equalsIgnoreCase(other.name)
          && this.age == other.age;
    }
  }
}
