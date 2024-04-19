# Executive Report

## Project Overview
The project involves the scheduling of MsC dissertation defenses, also known as viva. The objective of the project is to create a scheduling system that can efficiently allocate time slots for viva based on various constraints and restrictions.

The significance of this project lies in its ability to streamline the scheduling process for MsC dissertation defenses. By automating the scheduling process, it can save time and effort for the entities involved, such as the students, examiners, and other stakeholders. It can also help ensure that viva are scheduled at the most favorable times, taking into account the availability and preferences of the participants. Additionally, by considering the scheduling restrictions and the potential overlap of entities across different viva, the system can optimize the allocation of time slots and minimize conflicts.

Overall, the project aims to improve the efficiency and effectiveness of scheduling MsC dissertation defenses, making the process smoother and more convenient for all parties involved.

## Domain Model

The domain model for the Viva scheduling system is designed around the concept of roles and their operations. The key entities in this model are:

![Domain Model](diagrams/domainModel.png)

### Viva
A Viva instance represents an individual dissertation defense session. It contains information about the student presenting their dissertation, the title of the dissertation, and the members of the jury who will evaluate the defense. The jury members are represented by a list of Role instances.

### Role
The Role enum defines the different roles that individuals can have during a dissertation defense. These roles include:

- **President:** Typically the head of the examination panel.
- **Advisor:** The academic advisor who has guided the student throughout their dissertation.
- **CoAdvisor:** An additional advisor who may have provided support during the dissertation process.
- **Supervisor:** A supervisory role overseeing the dissertation defense process.
Each role has an associated resource, which could be a teacher or an external examiner.

### ResourceId
The ResourceId trait serves as the identifier for resources associated with roles. It is a common interface implemented by specific resource identifiers, such as TeacherId and ExternalId. These identifiers uniquely identify resources within the scheduling system.

### Resource
The Resource trait represents the resources available for scheduling dissertation defenses. It includes information such as the resource identifier (ResourceId), the name of the resource (e.g., the name of a teacher or an external examiner), and the availability of the resource for scheduling (Availability). Resources are categorized into subclasses Teacher and External, depending on whether they are internal faculty members or external examiners.

### Availability
The Availability class represents the availability of resources for scheduling dissertation defenses. It contains details such as the start and end times during which the resource is available and the preference level associated with scheduling the resource for a particular defense. This allows the scheduling system to optimize the allocation of resources based on their availability and preferences.

### ScheduledViva
A ScheduledViva instance represents a dissertation defense session that has been scheduled within the agenda. It includes details such as the student presenting their dissertation, the title of the dissertation, the members of the jury, the start and end times of the defense session, and the preference level associated with scheduling the defense.

### CompleteSchedule
The CompleteSchedule class encapsulates the entire schedule of dissertation defenses, including all scheduled defense sessions (ScheduledViva instances) and the total preference level for the entire schedule. It provides a comprehensive view of the scheduled defenses and their associated preferences, allowing stakeholders to assess the overall scheduling efficiency and effectiveness.

## Key Decisions & Justifications

This log lists the architectural decisions for MS01.

* [ADR-0001](decisions/0001-viva-model-use-role-list.md) - Should viva use as attribute, a set of roles or president and advisor roles?
* [ADR-0002](decisions/0002-role-operations-use-module.md) - Should role operations be a companion object or module?
* [ADR-0003](decisions/0003-role-model-use-resource-reference.md) - Should role model use object reference or id?
* [ADR-0004](decisions/0004-role-model-use-enum.md) - Should we use enum or sealed trait for roles?

## Challenges & Solutions
Discuss any challenges encountered during the project and how they were addressed.

## Future Improvements
Suggest potential improvements or next steps for the project.

## Conclusion
Summarize the project's achievements and its implications.
