/**
 *Made by: Stormblackbird
 *Version: 1.0.3
 *By order of: Hyperion Parks
 */

package com.zodiac;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleCollisionEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.zodiac.RideFrisbee;
import com.bergerkiller.bukkit.common.entity.CommonEntity;

public class Main extends JavaPlugin implements Listener {
	RideFrisbee RF = new RideFrisbee(this);
	closeOrOpenFence CF = new closeOrOpenFence(this);
	ArrayList listenForPlayers = new ArrayList();
	ArrayList playersInCart = new ArrayList();
	String rideName;
	String rideStartName;
	int rideIsJoining = 0;
	int rideHasStarted = 0;
	int runnableID;
	Location rideLocation;
	boolean delete = false;


	private static final HandlerList handlers = new HandlerList();
	public HandlerList getHandlers() {
	    return handlers;
	}
	public static HandlerList getHandlerList() {
		return handlers;
	}

	public void onEnable() {
		PluginManager pm = Bukkit.getServer().getPluginManager();
		pm.registerEvents(this, this);
		loadConfiguration();
		reloadConfig();
		if(delete){RFdelete();
		delete=false;}
	}

	public void onDisable() {
		if(RF != null){
			Bukkit.getScheduler().cancelTask(runnableID);
			delete = true;
			RFdelete();
			RFdelete();}
		saveDefaultConfig();
	}

	private void RFdelete() {
		RF.delete();
		rideHasStarted = 0;
		CF.getBlocksOpen(rideLocation, 10);
	}
	public void loadConfiguration() {
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender.isOp()) {
			if (cmd.getName().equalsIgnoreCase("zodiac")) {
				reloadConfig();
				if (args.length == 0) {
				}
				if (args.length == 2) {
					if (args[0].equalsIgnoreCase("set")) {
						if (!(listenForPlayers.contains(sender.getName()))) {
							listenForPlayers.add(sender.getName());
							sender.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "[Frisbee]" + ChatColor.GOLD
									+ " Right click the center floor block of the middle pole.");
							rideName = args[1];
						}
					}
					if (args[0].equalsIgnoreCase("delete")) {
						Bukkit.getScheduler().cancelTask(runnableID);
						rideHasStarted = 0;
						RF.delete();
					}
					if (args[0].equalsIgnoreCase("start")) {
						if (rideHasStarted != 1) {
							if (rideHasStarted != 2) {
								rideName = args[1];
								loadConfiguration();
								rideHasStarted = 1;
						        List rideLocationList = getConfig().getList("Frisbees." + rideName);

						        double x = Integer.parseInt(String.valueOf(rideLocationList.get(0))) + 0.5;
						        double y = Integer.parseInt(String.valueOf(rideLocationList.get(1)));
						        double z = Integer.parseInt(String.valueOf(rideLocationList.get(2))) + 0.5;
						        World worldName = Bukkit.getWorld(String.valueOf(rideLocationList.get(3)));

								rideLocation = new Location(worldName, x, y, z);
								CF.getBlocksOpen(rideLocation, 5);

								RF = new RideFrisbee(rideLocation);
								RF.spawn();
								
                                reloadConfig();
                                List<Double> heightList = (List<Double>) getConfig().getList("Heights");
                                List<Double> spinList = (List<Double>) getConfig().getList("Spins");
                                final double[] heightPercentArray = new double[heightList.size()];
                                final double[] spinArray = new double[heightList.size()];
                                for (int i = 0; i < heightList.size(); i++) {
                                    heightPercentArray[i] = heightList.get(i);
                                    spinArray[i] = spinList.get(i);
                                }
                                
								new BukkitRunnable() {
									int spin = 0;
									int heightSwing = 0;
									int spinSwing = 0;
								    
									int heightStage = 0;
									int spinStage = 0;
									
									@Override
									public void run() {
										runnableID = this.getTaskId();
										heightSwing %= 7200;
										if(heightSwing == 0 || heightSwing == 3600){
											heightStage += 1;
											//heightStage %= heightPercentArray.length;
											if(heightStage == 2){
												Bukkit.getScheduler().cancelTask(this.getTaskId());
												RF.delete();
												rideHasStarted = 0;
												return;
											}
										}
										
										if(spinSwing == 50 || spinSwing == 100 || spinSwing == 150 || spinSwing == 3450 || spinSwing == 3500 || spinSwing == 3550) {
											spinStage += 1;									
										}
										
										double pendulum = Math.sin(Math.toRadians(0.05 *heightSwing)) * 90;
										pendulum *= heightPercentArray[heightStage];
										RF.animate(pendulum, spin, 0);
										spin += spinArray[spinStage];
										heightSwing++;
										spinSwing++;
										if (heightPercentArray[heightStage] > 0){
											CF.getBlocksClose(rideLocation, 5);
										}

										//getLogger().info(RF.getVector());
									}
								}.runTaskTimer(this, 0, 1);
							}
							
						}
					}
				} else {
					sender.sendMessage(
							ChatColor.AQUA + "" + ChatColor.BOLD + "[Frisbee]" + ChatColor.GOLD + " Incorrect Format");
				}
			}
		} else {
			sender.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "[Frisbee]" + ChatColor.RED + " No Permission!");
		}
		return true;
	}

	@EventHandler
	public void blockClick(PlayerInteractEvent e) {
		reloadConfig();
		Player p = e.getPlayer();
		if (listenForPlayers.contains(p.getName())) {
			if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				Block b = e.getClickedBlock();
				ArrayList<String> locationList = new ArrayList<String>();
				locationList.add(String.valueOf(b.getLocation().getBlockX()));
				locationList.add(String.valueOf(b.getLocation().getBlockY()));
				locationList.add(String.valueOf(b.getLocation().getBlockZ()));
				locationList.add(String.valueOf(b.getLocation().getWorld().getName()));
				getConfig().set("Frisbees." + (rideName), locationList);
				saveConfig();
				listenForPlayers.remove(p.getName());
				p.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "[Frisbee]" + ChatColor.GOLD
						+ " Successfully added the ride '" + ChatColor.AQUA + "" + ChatColor.BOLD + rideName
						+ ChatColor.RESET + "" + ChatColor.GOLD + "'!");
			}
		}
	}

	@EventHandler
	public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {
		if (event.getVehicle().getCustomName() != null) {
			if (event.getVehicle().getCustomName().toLowerCase().startsWith("frisbeeminecart")) {
				event.setCollisionCancelled(true);
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
        if (event.getVehicle().getCustomName() != null) {
            if (event.getVehicle().getCustomName().toLowerCase()
                    .startsWith("frisbeeminecart")) {
                event.setCancelled(true);
            }
        }
    }

	@EventHandler
	public void armorStandHeadRemove(PlayerArmorStandManipulateEvent event) {
		if (event.getArmorStandItem().getAmount() == 2) {
			event.setCancelled(true);
		}
	}
}
