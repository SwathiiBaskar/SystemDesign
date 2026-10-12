package com.example.consistent_hash;

import org.springframework.stereotype.Component;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;

import com.example.consistent_hash.Model.Student;
import com.example.consistent_hash.Service.StudentService;

@Component
public class SampleDataLoader implements CommandLineRunner {
    
    private final StudentService studentService;
    
    @Value("${app.seed-data:false}")
    private boolean seedData;
    
    public SampleDataLoader(StudentService studentService) {
        this.studentService = studentService;
    }
    @Override
    public void run(String... args) {
        if (!seedData) {
            return;
        }
        List<Student> sampleStudents = List.of(
            new Student("S001", "Aditya Sharma", "CSE", 8.9),
            new Student("S002", "Bhavya Iyer", "ECE", 7.8),
            new Student("S003", "Chetan Rao", "MECH", 6.9),
            new Student("S004", "Divya Menon", "CSE", 9.1),
            new Student("S005", "Esha Kapoor", "IT", 8.2),
            new Student("S006", "Farhan Ali", "CIVIL", 7.1),
            new Student("S007", "Gauri Nair", "CSE", 8.6),
            new Student("S008", "Harish Kumar", "EEE", 7.4),
            new Student("S009", "Isha Verma", "IT", 8.8),
            new Student("S010", "Jatin Malhotra", "MECH", 6.5),
            new Student("S011", "Kavya Reddy", "CSE", 9.3),
            new Student("S012", "Lakshay Gupta", "ECE", 7.6),
            new Student("S013", "Meera Pillai", "CIVIL", 7.0),
            new Student("S014", "Naveen Joshi", "IT", 8.4),
            new Student("S015", "Ojas Deshmukh", "CSE", 8.1),
            new Student("S016", "Priya Chawla", "EEE", 7.9),
            new Student("S017", "Qadir Sheikh", "MECH", 6.7),
            new Student("S018", "Riya Bansal", "CSE", 9.0),
            new Student("S019", "Sameer Khan", "ECE", 7.3),
            new Student("S020", "Tanvi Saxena", "IT", 8.5),
            new Student("S021", "Uday Choudhary", "CIVIL", 6.8),
            new Student("S022", "Vidya Krishnan", "CSE", 8.7),
            new Student("S023", "Waseem Ahmed", "EEE", 7.2),
            new Student("S024", "Xitij Patel", "MECH", 6.6),
            new Student("S025", "Yamini Rao", "CSE", 9.2),
            new Student("S026", "Zoya Siddiqui", "IT", 8.3),
            new Student("S027", "Aakash Tiwari", "ECE", 7.5),
            new Student("S028", "Bindu Shetty", "CIVIL", 6.4),
            new Student("S029", "Charan Reddy", "CSE", 8.95),
            new Student("S030", "Deepa Nambiar", "EEE", 7.7)
            );
        System.out.println("Seeding " + sampleStudents.size() + " sample student records...");
        for (Student s : sampleStudents) {
            String node = studentService.addStudent(s);
            System.out.println("Seeded " + s.getId() + " -> " + node);
        }
    }
}