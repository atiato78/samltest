package io.fabric8.quickstarts.camel.chapters;

public class Params {


    private Integer chapter_id; 
    private String material_name;
    private String lang;
    private Integer instructor_id;
    private String chapter_number;
    private String chapter_subject;
    private String creation_date;

    public String getCreation_date() {
        return creation_date;
    }

    public void setCreation_date(String creation_date) {
        this.creation_date = creation_date;
    }

    public Integer getChapter_id() {
        return chapter_id;
    }

    public void setChapter_id(Integer chapter_id) {
        this.chapter_id = chapter_id;
    }

    public String getChapter_subject() {
        return chapter_subject;
    }

    public void setChapter_subject(String chapter_subject) {
        this.chapter_subject = chapter_subject;
    }
   



   

    public Integer getInstructor_id() {
        return instructor_id;
    }

    public void setInstructor_id(Integer instructor_id) {
        this.instructor_id = instructor_id;
    }

    public String getChapter_number() {
        return chapter_number;
    }

    public void setChapter_number(String chapter_number) {
        this.chapter_number = chapter_number;
    }

  



}