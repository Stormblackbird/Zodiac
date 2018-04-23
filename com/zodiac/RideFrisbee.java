package com.zodiac;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.bergerkiller.bukkit.common.controller.EntityController;
import com.bergerkiller.bukkit.common.entity.CommonEntity;
import com.bergerkiller.mountiplex.dep.net.sf.cglib.core.Block;

public class RideFrisbee {
	Main main;
	public RideFrisbee(Main main) {
		this.main = main;
	}
	Location rideLocation;
	//Yaw 0 = south, +z axis
	double height = 7;
	double radius = 7;
	int minecartCount = 20;
	Minecart[] minecarts;
	ArmorStand[] armies;
	int armorCount = (int) Math.ceil(height/0.5);
	double minecartRotation;
	double minecartIncrementDegrees;
	double phi = 2*Math.PI / minecartCount;
	public static final double ARMOR_STAND_HEAD = 0.5;
	public static final double ARMOR_STAND_OFFSET = 1.5;
	

	public RideFrisbee(Location rideLocation){
		this.rideLocation = rideLocation;
		minecartIncrementDegrees = 360/minecartCount;
	}
	
	public void spawn(){
		minecarts = new Minecart[minecartCount];
		armies = new ArmorStand[armorCount];
		World world = rideLocation.getWorld();
		Vector offset = new Vector(radius, 0, 0);
		
		
		ItemStack headMaterial = new ItemStack(Material.NOTE_BLOCK, 2);
        headMaterial.setAmount(2);

		
		for (int i = 0; i < minecartCount; i++) {
			Location mLocation = rideLocation.clone();
			Vector mOffset = VectorUtil.rotateY(offset.clone(), phi * i);
			mLocation.add(mOffset);
			mLocation.setYaw((float) (90 + (minecartIncrementDegrees)*i));
			mLocation.setY(mLocation.getY() + 4);

			//m.teleport(mLocation);
			//minecartOffsets[i] = new Location(mLocation.getWorld(), mOffset.getX(), mOffset.getY(), mOffset.getZ(), mLocation.getYaw(), 0);
			Minecart m = world.spawn(mLocation, Minecart.class);
			minecarts[i] = m;
			m.setCustomName("frisbeeMinecart" + i);
			CommonEntity<?> entity = CommonEntity.get(m);
			//m.teleport(mLocation);
			//m.setGravity(false);
			entity.setController(new EntityController<CommonEntity<?>>() {
				public boolean onEntityCollision(Entity entity) {
					return false; // True to allow, False to deny
				}

				public boolean onBlockCollision(
						Block block, BlockFace hitFace) {
					return false; // True to allow, False to deny
				}
			});
			entity.setPosition(mLocation.toVector().getX(), mLocation.toVector().getY(), mLocation.toVector().getZ());
		}
		for (int i = 0; i < armorCount; i++) {
			Location aLocation = rideLocation.clone();
        	aLocation.setZ(aLocation.getZ() - ARMOR_STAND_HEAD * i + ARMOR_STAND_OFFSET - 1);
        	aLocation.setY(aLocation.getY() );

			ArmorStand a = world.spawn(aLocation, ArmorStand.class);
			a.setGravity(false);
			a.setHelmet(headMaterial);
			a.setVisible(false);
			a.teleport(aLocation);
        	armies[i] = a;
		}
	}
	
	public void animate(double swing, double spin, double heightPercent){
		Vector rideVector = rideLocation.toVector();
		double currentHeight = height*(1-heightPercent);
		
		for (int i = 0; i < minecartCount; i++) {
			Minecart m = minecarts[i];
			Vector mVector = new Vector(radius, 0, 0);
			mVector = VectorUtil.rotateY(mVector, Math.toRadians(spin) + phi*i);
			
			mVector.setY(mVector.getY() - currentHeight);
			mVector = VectorUtil.rotateZ(mVector, Math.toRadians(swing));
			mVector.setY(mVector.getY() + currentHeight);
			mVector.add(rideVector);
			mVector.setY(mVector.getY() - 3);
			mVector.setZ(mVector.getZ() - 7);
			CommonEntity<?> cm = CommonEntity.get(m);
			cm.setLocation(mVector.getX(), mVector.getY(), mVector.getZ(), 90 + (float) (minecartIncrementDegrees*i + spin), 0);
			
		
		}
		for (int i = 0; i < armorCount; i++) {
			ArmorStand a = armies[i];
			
			Vector aVector = new Vector(0, currentHeight*(-1+((double)i)/armorCount), 0);
			aVector = VectorUtil.rotateZ(aVector, Math.toRadians(swing));
			aVector.setY(aVector.getY() + currentHeight - ARMOR_STAND_OFFSET);
			aVector.add(rideVector);
			aVector.setY(aVector.getY() - 6);
			aVector.setZ(aVector.getZ() - 6);
			CommonEntity<?> cm = CommonEntity.get(a);
			cm.setPosition(aVector.getX(), aVector.getY(), aVector.getZ());
		}
	}
	
	
	public void delete(){
		
		for (int i = 0; i < minecartCount; i++) {
			Minecart m = minecarts[i];
			m.remove();
		}
		for (int i = 0; i < armorCount; i++) {
			ArmorStand a = armies[i];
			a.remove();
		}
		
	}
	
	public String getVector() {
		ArmorStand a = armies[1];
		return a.getLocation().toString();
	}
	
}
