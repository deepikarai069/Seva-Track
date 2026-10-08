USE sevatrack;

INSERT INTO departments(id,name,code) VALUES
 (1,'Water Supply (Jal Kal)','WTR'),(2,'Roads & Infrastructure','RDS'),
 (3,'Sanitation & Waste','SAN'),(4,'Street Lighting & Electrical','ELC');

INSERT INTO wards(id,name) VALUES
 (1,'Hazratganj'),(2,'Aminabad'),(3,'Chowk'),(4,'Alambagh'),
 (5,'Gomti Nagar'),(6,'Indira Nagar'),(7,'Aliganj'),(8,'Charbagh');

INSERT INTO categories(id,name,department_id,default_priority) VALUES
 (1,'No water supply',1,'HIGH'),(2,'Pipeline leakage',1,'MEDIUM'),(3,'Contaminated water',1,'CRITICAL'),
 (4,'Pothole',2,'MEDIUM'),(5,'Damaged footpath',2,'LOW'),(6,'Waterlogging / road cave-in',2,'HIGH'),
 (7,'Garbage not collected',3,'MEDIUM'),(8,'Drain blockage',3,'HIGH'),(9,'Dead animal removal',3,'HIGH'),
 (10,'Street light not working',4,'MEDIUM'),(11,'Exposed live wire',4,'CRITICAL');

INSERT INTO sla_policy(priority,level,hours) VALUES
 ('CRITICAL',1,4),('CRITICAL',2,4),('CRITICAL',3,2),
 ('HIGH',1,24),('HIGH',2,12),('HIGH',3,6),
 ('MEDIUM',1,72),('MEDIUM',2,36),('MEDIUM',3,24),
 ('LOW',1,120),('LOW',2,72),('LOW',3,48);

-- id, name, email, dept, ward, level, max_load, emergency
INSERT INTO officers(id,name,email,department_id,ward_id,level,max_load,emergency) VALUES
 (101,'Ramesh Verma','ramesh.verma@sevatrack.in',1,1,1,8,0),
 (102,'Sunita Yadav','sunita.yadav@sevatrack.in',1,2,1,8,0),
 (103,'Imran Qureshi','imran.qureshi@sevatrack.in',1,5,1,6,1),
 (104,'Alok Mishra','alok.mishra@sevatrack.in',1,NULL,2,15,1),
 (105,'Dr. Kavita Srivastava','kavita.srivastava@sevatrack.in',1,NULL,3,30,1),
 (201,'Pankaj Tiwari','pankaj.tiwari@sevatrack.in',2,3,1,8,0),
 (202,'Neha Gupta','neha.gupta@sevatrack.in',2,4,1,8,0),
 (203,'Sanjay Rawat','sanjay.rawat@sevatrack.in',2,6,1,6,1),
 (204,'Rakesh Chaudhary','rakesh.chaudhary@sevatrack.in',2,NULL,2,15,1),
 (205,'Anil Kapoor','anil.kapoor@sevatrack.in',2,NULL,3,30,1),
 (301,'Mohit Saxena','mohit.saxena@sevatrack.in',3,7,1,10,0),
 (302,'Pooja Pandey','pooja.pandey@sevatrack.in',3,8,1,10,0),
 (303,'Farhan Siddiqui','farhan.siddiqui@sevatrack.in',3,1,1,6,1),
 (304,'Meera Dubey','meera.dubey@sevatrack.in',3,NULL,2,15,1),
 (305,'Suresh Agarwal','suresh.agarwal@sevatrack.in',3,NULL,3,30,1),
 (401,'Deepak Singh','deepak.singh@sevatrack.in',4,5,1,8,0),
 (402,'Shweta Mehrotra','shweta.mehrotra@sevatrack.in',4,6,1,8,0),
 (403,'Vivek Chauhan','vivek.chauhan@sevatrack.in',4,2,1,6,1),
 (404,'Anjali Bajpai','anjali.bajpai@sevatrack.in',4,NULL,2,15,1),
 (405,'Rajiv Nigam','rajiv.nigam@sevatrack.in',4,NULL,3,30,1);
