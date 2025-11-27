Versions
Date	Version	In charge	Description
25-October 24	V1.0	Ngoc Tran, A Nguyen	Update Section I, II and Appendix A
			

 
Abbreviation

DMS	Database Management System
…	…

 
List of Figures
Figure 1 The accuracy between X and Y	10

 
List of Tables

Table 1: A comparison between X and Y	10

 
Table of Contents

I….	3
II…	4
III….	5




 
(Removing this italic red paragraph below after you read it)
Student please use font Times New Roman, size 13, Justify Text Alignment for the report content.
If you insert the code into the report, please keep its original format with font, size and colors.
Please insert only the code useful for the report in clarification, and limit lengthening the report by pasting unnecessary code. You can insert the path of the code files, and I will search it in your attached project code to check them.

I.	INTRODUCTION

[In this section, please insert a description of the business requirements of the project you are working on. You can play the rold of the customer who order this management software and write down the details of the requirements.

For example: In this project, we investigate the business functions of the hotel management system, and provide a desktop application supporting users….

This project provides the basic functions for the stakeholders/users as follows:
-	Input the customer information.
-	Input the booking.
-	Search customers
-	Display the booking details.
-	…

1 user role table
1 usecase diagram

In this project, we use Java OOP for designing classes and applying the OOP such as encapsulation, inheritance, … [You can update this section during the regular request and submission, and make it complete for the final submissions ]

II.	CLASS ANALYSIS

1.	Objects
-	In this section, students analyse and list objects (name, behavior, state)
-	Group objects  having common states and behaviors
No	Object Name	State	Behaviours
1	Nurse A	Nguyen Thi A, 35, General Diagnosis Department, Full Time	isWorking, isPaid, ….
2	Doctor 1		
Table1. List of Objects (Nurses, Doctors, Patients)

2.	Classes
-	Create classes (name, attributes, functions) for each group. List the classes accordingly.
-	You can use the table containing objects and their class.
-	Analyse the inheritance among classes, Abstract classes
-	Draw a diagram to show the inheritance (Note: at the analysis stage, please do not provide the details of the classes such as data types, variable names, return datatype of methods and the param lists of the methods)

III.	CLASS DESIGN

1.	Classes
-	Add a Class diagram: relationship among classes 


Figure 2. Class Diagram of Project …..

-	Add a table (provides more details)


No	Class	Instance Variable	Methods	Description
1	Staff
	1.	Public String fullname; 
-	It is public because the other classes or in the other packages cannot read this personal information 
Private Int age
Public String emailaddr;	1.	Boolean getFullname(string staffID)
-	This function is used to get the fullname of the staff and return the success (true) or failure (false).
2.	Boolean setFullname(string sID)
-	This function is used to …
3.		This class is used for managing the staff information and behaviors.
2				
Table 2. Details of Classes

-	For each class, design the detailed members. Students please choose the method/class/variable type for each class: public, private, default, final, static,… with explanation.


-	Abstract classes
Create a table of abstract classes
No	Abstract Class	Abstract Methods	Concrete Methods	Decription
1	PersonInformation	setInfo()	Util()	This class is an abstract class that is used by subclasses A, B, C, X, Y, Z, …. (See Figure 2.)
2				

2.	Some OOP techniques
2.1.Overloading method: 
	methods 1, 2,3 in class 1
Attached the class code if it is possible.
(Add some code of some significant classes you think there is a need)
abstract class 1{//abstract class
…. //overloading methods
// call-by-value
}
….

	methods 4, x, y in class 2
	..
2.2. Overriding method:
	methods 11, 12, 13 in class 1
	..



3.	 Inheritance
-	Present which inheritance-related techniques you use in those classes
-	Representative code
-	…

… 3 tier model…

IV.	Package Design
-	Analyse the package hierarchy used in your project.
-	…

V.	Interface Design
-	Design interface you use in your project.
-	… 

VI.	Access Control
-	Analyse and discuss on the access control relating variables (data), method,  classes and packages, interface.
Table X1: Data Access Control Table
No	Data	Class	Modifier	Description
1	Name, Department	Student	Public	•	The name of students can be accessed by the other classes inside or outside the package of Student.
•	Name can be public as it is not private information. It does not cause the serious consequence if the others know the name of student.
•	Classes A1, A2, A3,…can access these data.
2	Age, Mailing address	Student	Private	
…				

Table X2: Method Access Control Table
No			
1			
2			
…			
….
Note: students can compare or clarify using the tables or figures, whenever you insert a table or figure, you have to have at least one paragraph to discuss on their content.
…


VII.	Encapsulation vs Inheritance vs Polymorphism
[In this section, students will discuss on the three OOP features: Encapsulation, Polymorphism, Inheritance by in each part, you give the sample from your project code and explain why do you think those sample relates to the above three features.]
1.	Encapsulation
…
2.	Inheritance
…
3.	Polymorphism
…

VIII.	Experiment
1.	Environment and Tools
a.	Environment: Describe the physical resources (numbers of PCs, CPU, RAM, …)
b.	Tools: List tools, libraries you use for your project in here. If that tool is so new, please add a section Appendix to instruct how to install the tools.
c.	…
2.	Project functions
-	List and describe a bit the functions of the project
o	Search the students by their Department, Total Grade, ….
o	Input the Exam registration: student ID, Module, Exam date, ….
o	…
-	Student can provide a table for this section
-	…
3.	Database (min. 4 tables)
-	Using flat file (.txt, .csv, …) or database (MySQL, MS SQL, …)
-	ERD diagram
-	Data Diagram (tables if you use relational database system management, the text files you use to store the data, …) - 5-7 classes, max. 10 classes
-	…
4.	GUI (4 figures)
-	List a table of the name and the orders of  user interfaces in the projects
Figure 1: Student Input Dialog
……(paste the figure 1 in here)…
(explain the function of the buttons, and the button-related event, what happen if I press that button? When a user presses the button “Input”, an ID is automatically generated for that student and his/her information (i.e., Name, Department, ….) is stored to the table Student in the database, through the method inputStudent(…) of the class StudentINfoInteraction.

-	Paste the figures of user interfaces/dialogs/webpages in here according to the order listed in the above table. 
o	For each figure, describe its functions, and its GUI components on the user interface, how to use it. Should give examples.
o	…
-	... 

IX.	Conclusion
-	Assess your project by discussing on its pros and the cons.
-	I want to hear what you really think about your project quality, and leave a score you think your project can gain along with the reason why you think so.
-	What more function you think you can improve and add to the project in the future.
-	…
 
DUTY ROSTER

ID	Task 	In Charge	Start	End	State	Note
1	Design Class A, B, C	Nguyen Van A
Le Thi B	2-Dec-18	1-Jan-19	Done	
2	Code Function 1	Nguyen Van A	01-Jan-19	17-Jan-19	Done	
3	Report Section II	Le Thi B	02-Jan-19		In progress	
…	…	…	…	…	…	…
n	…	…	…	…	…	…

 
REFERENCE

1.	Tutorial Page, Oracle, 2024, https://...
2.	…


[Students, please put here whatever sources you referred or used in here]
 
APPENDIX A: CLASS DESCRIPTION
-	Class 1 : Grade…. (Source: Src/Grade.java)
-	
