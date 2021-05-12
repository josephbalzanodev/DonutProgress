# DonutProgress 🚀
 
 [![](https://jitpack.io/v/josephbalzanodev/DonutProgress.svg)](https://jitpack.io/#josephbalzanodev/DonutProgress)

Now this readme is very poooor 😄

## To implement
As usual 😒 
- in project `build.gradle`:
``` 
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
- in app `build.gradle`:
``` 
	dependencies {
	        implementation 'com.github.josephbalzanodev:DonutProgress:0.1'
	}
```

## Useee it 🎉
```
        <it.jbalzano.graphics.DonutProgress
            android:id="@+id/donut"
            android:layout_width="250dp"
            android:layout_height="250dp"
            android:layout_margin="24dp"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:min="0"
            app:max="90"
            app:thickness="36dp"
            app:ticks="30,60,90" 
            app:overlapLast="false"
            app:bgcolor="#33A9ACA9"
            app:colors="#C3C3E6,#BBA0CA,#B370B0"
            app:icons="ic_car,ic_subway,ic_boat"/>
```
![is only a sample](https://github.com/josephbalzanodev/DonutProgress/raw/main/sample.png)
