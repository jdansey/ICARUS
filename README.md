# ICARUS
Purpose
The purpose of this project is to investigate and prototype the use of a constraint-based scheduling algorithm for a complex data set.  Historically, our competitors have used “brute force” scheduling algorithms in our domain resulting in slow, non-deterministic, and/or inefficient schedules.  BITS believes that a constraint-based scheduling algorithm will greatly improve the speed and quality of the resulting schedules.  For this project, the intern will research, summarize, and present an overview of constraint-based scheduling algorithms, current implementation techniques, and a comparative analysis with other scheduling algorithm solutions.  Additionally, the intern will work with his mentor to model and document the constraints for an analog of our domain and develop a prototype that implements the identified constraints.  This development will add constraints in an iterative manner.

Analog
There are cell phones with given emitter areas that are in need of service. The area in which a cell phone can get service is determined by topographical data, frequency and height. In order to service a cell phone, a cell tower must be placed so that its service area overlaps with the cell phone emitter area. Each cell phone has a given daily and weekly minimum time serviced constraint. An example of this is CellPhone A must get at least 8 hours of service a day. Some cell phones also have scheduled constraints where they are given preferred service over others. An example of a scheduled constraint is CellPhone B must get service between 10-3 on a Wednesday. The goal is to create a schedule that places cell tower(s) such that all cell phone constraints get met.

Development
Model and implement identified constraints and scheduling algorithm.  Develop sufficient displays and/or logs to allow insight into the inputs, execution, and results of the scheduling algorithm.

References
http://www.math.unipd.it/~frossi/cp-school/lepape.pdf
http://www2.parc.com/isl/members/fromherz/publications/cbs-tutorial-local.pdf
This list is not exhaustive, just a starting point.

