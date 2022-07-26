package org.opensearch.graph.generator.model.entity;





import org.opensearch.graph.generator.model.enums.Gender;

import java.util.Date;

/**
 * Created by benishue on 15-May-17.
 */
public class Person extends EntityBase {

    //region Getters & Setters
    public String getName() {
        return firstName +" " + lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public Date getDeathDate() {
        return deathDate;
    }

    public void setDeathDate(Date deathDate) {
        this.deathDate = deathDate;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
    //endregion

    //region Public Methods
    @Override
    public String toString() {
        return "Person{" +
                "id='" + getId() + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", gender=" + gender +
                ", birthDate=" + birthDate +
                ", deathDate=" + deathDate +
                ", height=" + height +
                '}';
    }

    @Override
    public String[] getRecord() {
        return new String[]{
                this.getId(),
                this.firstName,
                this.lastName,
                this.gender.toString(),
                Long.toString(this.birthDate.getTime()),
                Long.toString(this.deathDate.getTime()),
                Integer.toString(this.height),
                this.getName()
        };
    }
    //endregion

    //region Fields
    private String firstName;
    private String lastName;
    private Gender gender;
    private Date birthDate;
    private Date deathDate;
    private int height;
//    private boolean isOldestOffspring ;
//    private boolean isAlive ;
//    private int parentId ;
//    private int kingdom_id ;
//    private List<Integer> offspringsIdList ;
//    private String personInfo ;
//    private Date since;
//    private Date till;
//    private int birthYear ;
    //endregion

    //region Builder
    public static final class Builder {
        private String id;
        private String name;
        private String firstName;
        private String lastName;
        private Gender gender;
        private Date birthDate;
        private Date deathDate;
        private int height;

        private Builder() {
        }

        public static Builder get() {
            return new Builder();
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder withGender(Gender gender) {
            this.gender = gender;
            return this;
        }

        public Builder withBirthDate(Date birthDate) {
            this.birthDate = birthDate;
            return this;
        }

        public Builder withDeathDate(Date deathDate) {
            this.deathDate = deathDate;
            return this;
        }

        public Builder withHeight(int height) {
            this.height = height;
            return this;
        }

        public Person build() {
            Person person = new Person();
            person.setId(id);
            person.setFirstName(firstName);
            person.setLastName(lastName);
            person.setGender(gender);
            person.setBirthDate(birthDate);
            person.setDeathDate(deathDate);
            person.setHeight(height);
            return person;
        }
    }
    //endregion

}
