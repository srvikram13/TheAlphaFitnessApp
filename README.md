# TheAlphaFitnessApp
CS175-02 The Alpha Fitness App. Part of Homework assignment 2

Planned Class Structure:

utils
	InputFilterMinMax.java

model
	WorkoutInfo.java
		- will contain getter methods for data

controller
	MyService.java
	StepCounterService.java
		- will contain calorie calculation & storing logic
    
views
	MainActivity.java

	ProfileActivity.java
		- will fetch calorie info and update UI
	RecordWorkout.java
		- will fetch user's route and update on map
	WorkoutDetails.java
		- will fetch calorie and step info and plot
