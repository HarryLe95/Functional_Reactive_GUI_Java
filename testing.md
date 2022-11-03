# Testing
The following features were tested:

- Clear display after 3s if no new update is provided.
- Set filter bounds correctly.
- Filter streamed data based on filter. 
- Automatically add and remove data in a time window.
- Calculate distance based on events that satisfy constraints

## Test Setup:
Using `GUITest.java` script, using the provided parameters in the constructor in 
the main function, the test is as follows:

- Simulated `GpsService` stream is fired every `(id+1)*0.5` seconds, whose `latitude` and `longitude` are `id` times 10, 
attitude set to `1 x time.sample()` meters for every event. 
  - Tracker 1 has `lat=long=0` and is fired every 0.5s.
  - Tracker 2 has `lat=long=10` and is fired every 1s.
  - ... 
  - Tracker 9 has `lat=long=90` and is fired every 5s. 

## Test 1 - clear display after 3s:

### Test 1.1

- Set `LatLB` to 51 and `LatUB` to 60 to track only tracker 6 in the display.
- Observe that `FilteredEvent` label is cleared after 3s.
- The reason is that Tracker 6 is fired every 3.5 seconds, so the filtered display will be 
cleared for 0.5s before it is updated. 

### Test 1.2
- Set `LatLB` to 11 and `LatUB` to 20 to track only tracker 1.
- Observe that `FilteredEvent` is never cleared.
- This is because Tracker 1 data is fired every 1 second so there is no 3s to clear.

### Test 1.3

- Set `LatLB` to 61 and `LatUB` to 70 to track only tracker 7 in the display.
- Observe that `FilteredEvent` label is cleared after 3s.
- The reason is that Tracker 7 is fired every 4 seconds, so the filtered display will be
  cleared for 1s before it is updated.


## Test 2 - set filter bounds:
We want to test that 
- A valid entry is entered for each text field - i.e. 
  - Latitude can only be in the range `[-90,90]` and Longitude in `[-180,180]`. Values set outside 
  this range or values incorrectly parsed are automatically saturated to the bound values. If LatLB is set to -100, pressing `SetBound`
  updates the corresponding label to -90.
  - If entered lower-bound is higher than upperbound, pressing `SetBound` button swaps the bound values. 

### Test 2.1
- Enter Lat (110,120).
- Expect (90,90)
- Observed (90,90)
- Correct since LatBounds can only be as high as 90.

### Test 2.2
- Enter Lon (110,120).
- Expect (110,120)
- Observed (110,120)
- Correct since valid entries were provided.

### Test 2.3
- Enter Lat(rtf,asd)
- Expected (-90,90)
- Observed (-90,90)
- Correct since providing invalid entries will reset to max bounds.

### Test 2.4
- Enter Lat(15,10)
- Expected (10,15)
- Observed (10,15)
- Correct since if the bounds are not in the correct order, the values are swapped.

## Test 3 - filter data correctly
We want to test that the correct filtered stream data is shown on display. This can be 
done simply by narrowing the right bounds and check that the right tracks are display, since 
each track's latitude and longitude are regularly spaced: 

### Test 3.1
- Set Lat(11,20), Lon(-180,180)
- Expected - Track 2 to be shown
- Observed - Track 2.
- Correct since `lat=long=20` for Track 2.

### Test 3.2
- Set Lat(11,40), Lon(31,40)
- Expected - Track 4 to be shown
- Observed - Track 4
- Correct since `lat=long=40` for Track 4.

### Test 3.3
- Set Lat(11,40), Lon(41,50)
- Expected - empty
- Observed  - empty
- Correct since no track satisfies this filter.

### Test 3.4
- Set Lat(41,60), Long(-180,180)
- Expected - Track 5 and 6
- Observed - Track 5 and 6

## Test 4 - correctly add and remove data within a specified interval

Using the default parameter, data is cleared every 5seconds. Since each tracker 
is moving on a straight vertical line, total distance travelled in 5s is simply 
`tlast-tfirst` meters where `tlast` is the timestamp of the last event and `tfirst` is 
the timestamp of the first event in a 5 s window:

### Test 4.1 
- Look at distance of tracker 1
- Expect - 4.5
- Output - 4.5
- Correct since tracker 1 is updated every 1s, observations with timestamp `t` and `t-5`
are both in the window.

### Test 4.2
- Look at distance of tracker 2
- Expect - 4.5
- Output - 4.5
- Correct since tracker 2 is updated every 1.5s, observations in a 5 seconds window have TS
`[t, t-1.5, t-3.0, t-4.5]`. Hence, distance is `t-t+4.5=4.5`

### Test 4.3
- Look at distance of tracker 3
- Expect - 4.0
- Output - 4.0
- Correct since tracker 3 is updated every 2s, observations in a 5 seconds window have TS
  `[t, t-2, t-4]`. Hence, distance is `t-t+4.0=4.0`

### Test 4.4
- Look at distance of tracker 4
- Expect - 2.5
- Output - 2.5
- Correct since tracker 4 is updated every 2.5s, observations in a 5 seconds window have TS
  `[t, t-2.5]`. Hence, distance is `t-t+2.5=2.5`

### Test 4.5
- Look at distance of tracker 5
- Expect - 3.0
- Output - 3.0
- Correct since tracker 5 is updated every 3s, observations in a 5 seconds window have TS
  `[t, t-3.0]`. Hence, distance is `t-t+3.0=3.0`

### Test 4.6
- Look at distance of tracker 6
- Expect - 3.5
- Output - 3.5
- Correct since tracker 6 is updated every 3.5s, observations in a 5 seconds window have TS
  `[t, t-3.5]`. Hence, distance is `t-t+3.5=3.5`

### Test 4.7
- Look at distance of tracker 7
- Expect - 4.0
- Output - 4.0
- Correct since tracker 7 is updated every 4.0s, observations in a 5 seconds window have TS
  `[t, t-4.0]`. Hence, distance is `t-t+4.0=4.0`

### Test 4.8
- Look at distance of tracker 8
- Expect - 4.5
- Output - 4.5
- Correct since tracker 8is updated every 4.5s, observations in a 5 seconds window have TS
  `[t, t-4.5]`. Hence, distance is `t-t+4.5=4.5`

## Test 5 - calculate distance based on filtered events 
For this task, I use a modified test script - `GUITest_Filtered`, in which event is sent every 
1s and longitude jumps in a range [0.0, 1.0, 2.0, 3.0, 4.0] while latitude and altitude stays constant.
Distance travelled between consecutive points is 111km. Hence in a window of 5s, the total distance travelled can be:
`111 + 111 + 111 + 444 = 777` and `111+111+111+111=444` where `444` is the distance travelled from a point at longitude 4.0 to 0.0. 
Hence we shall test the filter by at different constraint 

### Test 5.1 
Set LongLB to -180, LongUB to 180: 
- Expect distance - 444km and 777km
- Output - 444km and 777km
Test passed since all events satisfy the constraint  

### Test 5.2 
Set LongLB to 10, LongUB to 180:
- Expect distance - 0km
- Output - 0km 
Test passed since no event satisfies the constraint 

### Test 5.3
Set LongLB to 0, LongUB to 1
- Expect distance - 111km 
- Output - 111km 
Test passed since the two events where distance is computed are from 0 to 1 and from 1 to 0

### Test 5.4
Set LongLB to 0, LongUB to 2
- Expect distance - 222km and 333km 
- Output- 222km and 000km
Test passed since the events where distance is computed are from 0 to 1 to 2 - distance of 222km and 
event from 1 to 2 to 0 - distance of 333km. 









