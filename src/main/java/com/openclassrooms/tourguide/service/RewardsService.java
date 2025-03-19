package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.helper.Toolkit;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.openclassrooms.tourguide.helper.Toolkit.TEST_MODE;

@Service
public class RewardsService {


	// proximity in miles
    private int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;
	private final GpsUtil gpsUtil;
	private final RewardCentral rewardsCentral;
	
	public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
		this.gpsUtil = gpsUtil;
		this.rewardsCentral = rewardCentral;
	}
	
	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}
	
	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}

	public void calculateMultipleRewards(List<User> users) {
		int progress = 0;
		int total = users.size();

		try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
			List<? extends Future<?>> futures = users.stream()
					.map(user -> executor.submit(() -> { // Run each User reward calculation on a separate virtual thread
						calculateRewards(user);
					}))
					.toList();
			for (Future<?> future : futures) { // Wait for all virtual thread to complete his assigned task
				try {
					future.get();
					if (TEST_MODE) { // test mode only to see the progress in percent
						progress++;
						System.out.println("calculateMultipleRewards | Progress " + progress + " / " + total + " (" + (int)((float)progress/(float)total*100.0f) + "%)");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace(); // executor exceptions
		}
	}
	public void calculateRewards(User user) {
		List<VisitedLocation> userLocations = user.getVisitedLocations();
		List<Attraction> attractions = gpsUtil.getAttractions();

		// iterate backward with bounds Checking, allowing objects removal while looping without causing 'memory segment fault'
		for (int i=userLocations.size()-1; i>=0; i--) {
			if (i<userLocations.size()) { // bounds check
				VisitedLocation visitedLocation = userLocations.get(i);
				for (int j=attractions.size()-1; j>=0; j--) {
					if (j<attractions.size()) { // bounds check
						Attraction attraction = attractions.get(j);
						if(nearAttraction(visitedLocation, attraction)) { // in-range check
							user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
						}
					}
				}
			}
		}
	}
	
	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return Toolkit.getLocationsDistance(attraction, location) > attractionProximityRange ? false : true;
	}
	
	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return Toolkit.getLocationsDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
	}
	
	public int getRewardPoints(Attraction attraction, User user) {
		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	}

}
