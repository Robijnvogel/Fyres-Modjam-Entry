package fyresmodjam.worldgen;

import java.awt.Color;
import java.util.HashMap;

import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;
import cpw.mods.fml.common.FMLCommonHandler;
import fyresmodjam.ModjamMod;

public class FyresWorldData extends WorldSavedData {

	public static String[] validDisadvantages = {"Tougher Mobs", "Weak", "Explosive Traps", "Increased Mob Spawn", "Neverending Rain", "Neverending Night", "Permadeath"};
	public static String[] disadvantageDescriptions = {"Hostile enemies takes 25% less damage", "-25% melee damage", "Traps also trigger explosions when set off", "+33% hostile mob spawn rate", "Constantly rains", "Constant night", "Items dropped upon death are permanently lost"};

	public static String[] validTasks = {"Kill", "Burn"};

	public static String key = "FyresWorldData";

	public int[] potionValues = null;
	public int[] potionDurations = null;

	public int[][] mushroomColors = null;

	public String currentDisadvantage = null;

	public String currentTask = null;
	public int currentTaskID = -1;
	public int currentTaskAmount = 0;
	public int progress = 0;
	public int tasksCompleted = 0;
	public int rewardLevels = -1;

	public boolean enderDragonKilled = false;

	@SuppressWarnings("rawtypes")
	public static Class[] validMobs = {EntityDragon.class, EntityGhast.class, EntityWither.class};

	public static String[] validMobNames = {"Ender Dragon", "Ghast", "Wither"};
	public static int[][] mobNumbers = {new int[] {1, 1}, new int[] {5, 15} , new int[] {1, 1}};

	public HashMap<String, String> blessingByPlayer = new HashMap<String, String>();
	public HashMap<String, int[]> potionKnowledgeByPlayer = new HashMap<String, int[]>();

	public HashMap<String, NBTTagCompound> killStatsByPlayer = new HashMap<String, NBTTagCompound>();
	public HashMap<String, NBTTagCompound> weaponStatsByPlayer = new HashMap<String, NBTTagCompound>();
	public HashMap<String, NBTTagCompound> craftingStatsByPlayer = new HashMap<String, NBTTagCompound>();

	public static ItemStack[] validItems = {new ItemStack(Blocks.diamond_block), new ItemStack(Blocks.gold_block), new ItemStack(Blocks.emerald_block), new ItemStack(Blocks.lapis_block), new ItemStack(Items.diamond), new ItemStack(Items.emerald), new ItemStack(Items.gold_ingot), new ItemStack(Items.nether_star), new ItemStack(Items.ghast_tear)};

	public FyresWorldData() {
		super(key);
	}

	public FyresWorldData(String key) {
		super(key);
	}

	public static FyresWorldData forWorld(World world) {

		MapStorage storage = world.perWorldStorage;
		FyresWorldData result = (FyresWorldData) storage.loadData(FyresWorldData.class, key);

		if(result == null) {result = new FyresWorldData(); storage.setData(key, result); result.checkWorldData();}

		return result;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		if(nbttagcompound.hasKey("values")) {potionValues = nbttagcompound.getIntArray("values");}
		if(nbttagcompound.hasKey("durations")) {potionDurations = nbttagcompound.getIntArray("durations");}
		if(nbttagcompound.hasKey("currentDisadvantage")) {currentDisadvantage = nbttagcompound.getString("currentDisadvantage");}

		if(nbttagcompound.hasKey("currentTask")) {currentTask = nbttagcompound.getString("currentTask");}
		if(nbttagcompound.hasKey("currentTaskID")) {currentTaskID = nbttagcompound.getInteger("currentTaskID");}
		if(nbttagcompound.hasKey("currentTaskAmount")) {currentTaskAmount = nbttagcompound.getInteger("currentTaskAmount");}
		if(nbttagcompound.hasKey("progress")) {progress = nbttagcompound.getInteger("progress");}
		if(nbttagcompound.hasKey("tasksCompleted")) {tasksCompleted = nbttagcompound.getInteger("tasksCompleted");}

		if(nbttagcompound.hasKey("enderDragonKilled")) {enderDragonKilled = nbttagcompound.getBoolean("enderDragonKilled");}

		if(nbttagcompound.hasKey("rewardLevels")) {rewardLevels = nbttagcompound.getInteger("rewardLevels");}

		mushroomColors = new int[13][];
		for(int i = 0; i < 13; i++) {
			if(nbttagcompound.hasKey("mushroomColors_" + (i + 1))) {mushroomColors[i] = nbttagcompound.getIntArray("mushroomColors_" + (i + 1));}
		}

		if(nbttagcompound.hasKey("TempPlayerStats")) {
			NBTTagCompound tempStats = nbttagcompound.getCompoundTag("TempPlayerStats");

			for(Object o : tempStats.func_150296_c()) {
				if(o == null || !(o instanceof NBTTagCompound)) {continue;}
				NBTTagCompound player = (NBTTagCompound) o;

				blessingByPlayer.put(player.getString("Name"), player.getString("Blessing"));
				potionKnowledgeByPlayer.put(player.getString("Name"), player.getIntArray("PotionKnowledge"));
				if(player.hasKey("KillStats")) {killStatsByPlayer.put(player.getString("Name"), player.getCompoundTag("KillStats"));}
				if(player.hasKey("WeaponStats")) {weaponStatsByPlayer.put(player.getString("Name"), player.getCompoundTag("WeaponStats"));}
				if(player.hasKey("CraftingStats")) {craftingStatsByPlayer.put(player.getString("Name"), player.getCompoundTag("CraftingStats"));}
			}
		}

		checkWorldData();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		checkWorldData();

		nbttagcompound.setIntArray("values", potionValues);
		nbttagcompound.setIntArray("durations", potionDurations);
		nbttagcompound.setString("currentDisadvantage", currentDisadvantage);

		nbttagcompound.setString("currentTask", currentTask);
		nbttagcompound.setInteger("currentTaskID", currentTaskID);
		nbttagcompound.setInteger("currentTaskAmount", currentTaskAmount);
		nbttagcompound.setInteger("progress", progress);
		nbttagcompound.setInteger("tasksCompleted", tasksCompleted);

		nbttagcompound.setBoolean("enderDragonKilled", enderDragonKilled);

		nbttagcompound.setInteger("rewardLevels", rewardLevels);

		for(int i = 0; i < 13; i++) {nbttagcompound.setIntArray("mushroomColors_" + (i + 1), mushroomColors[i]);}

		if(!blessingByPlayer.isEmpty()) {
			NBTTagCompound tempPlayerStats = new NBTTagCompound();

			for(String s : blessingByPlayer.keySet()) {
				if(s == null) {continue;}

				NBTTagCompound player = new NBTTagCompound();

				player.setString("Name", s);
				player.setString("Blessing", blessingByPlayer.get(s));
				player.setIntArray("PotionKnowledge", potionKnowledgeByPlayer.get(s));
				if(killStatsByPlayer.containsKey(s)) {player.setTag("KillStats", killStatsByPlayer.get(s));}
				if(weaponStatsByPlayer.containsKey(s)) {player.setTag("WeaponStats", weaponStatsByPlayer.get(s));}
				if(craftingStatsByPlayer.containsKey(s)) {player.setTag("CraftingStats", craftingStatsByPlayer.get(s));}

				tempPlayerStats.setTag(s, player);
			}

			nbttagcompound.setTag("TempPlayerStats", tempPlayerStats);
		}
	}

	private void checkWorldData() {
		if(potionValues == null) {
			potionValues = new int[] {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};

			for(int i = 0; i < 12; i++) {
				int i2 = ModjamMod.r.nextInt(Potion.potionTypes.length);

				boolean stop = false;
				while(Potion.potionTypes[i2] == null || !stop) {
					stop = true;
					i2 = ModjamMod.r.nextInt(Potion.potionTypes.length);

					for(int i3 = 0; i3 < 12; i3++) {
						if(potionValues[i3] == i2) {stop = false; break;}
					}
				}

				potionValues[i] = i2;
			}
		} else {
			for(int i = 0; i < 12; i++) {
				if(Potion.potionTypes[potionValues[i]] == null) {
					int i2 = ModjamMod.r.nextInt(Potion.potionTypes.length);
					while(Potion.potionTypes[i2] == null) {i2 = ModjamMod.r.nextInt(Potion.potionTypes.length);}
					potionValues[i] = i2;
				}
			}
		}

		if(potionDurations == null) {potionDurations = new int[12];}
		for(int i = 0; i < 12; i++) {if(potionDurations[i] != 0) {continue;} potionDurations[i] = 5 + ModjamMod.r.nextInt(26);}

		if(mushroomColors == null) {
			mushroomColors = new int[13][2];

			for(int i = 0; i < 13; i++) {
				mushroomColors[i][0] = Color.HSBtoRGB(ModjamMod.r.nextFloat(), ModjamMod.r.nextFloat(), ModjamMod.r.nextFloat());
				mushroomColors[i][1] = Color.HSBtoRGB(ModjamMod.r.nextFloat(), ModjamMod.r.nextFloat(), ModjamMod.r.nextFloat());
			}
		}

		boolean changeDisadvantage = currentDisadvantage == null;

		if(!changeDisadvantage) {
			boolean valid = false;
			for(String s : validDisadvantages) {if(s.equals(currentDisadvantage)) {valid = true; break;}}
			changeDisadvantage = !valid && !currentDisadvantage.equals("None");
		}

		if(changeDisadvantage) {
			currentDisadvantage = validDisadvantages[ModjamMod.r.nextInt(validDisadvantages.length)];

			MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
			while(server != null && server.isHardcore() && currentDisadvantage.equals("Permadeath")) {currentDisadvantage = validDisadvantages[ModjamMod.r.nextInt(validDisadvantages.length)];}
		}

		if(currentTask == null) {
			giveNewTask();
		} else {
			boolean changeTask = true;
			for(String s : validTasks) {if(s.equals(currentTask)) {changeTask = false; break;}}
			if(changeTask || (currentTask != null && currentTask.equals("Kill") && currentTaskID == 0 && enderDragonKilled)) {giveNewTask();} else {if(currentTask.equals("Kill")) {currentTaskID %= validMobs.length;}}
		}

		if(rewardLevels == -1) {rewardLevels = 5 + ModjamMod.r.nextInt(6);}
	}

	public void giveNewTask() {
		progress = 0;

		currentTask = validTasks[ModjamMod.r.nextInt(validTasks.length)];

		if(currentTask.equals("Kill")) {
			currentTaskID = !enderDragonKilled ? ModjamMod.r.nextInt(validMobs.length) : 1 + ModjamMod.r.nextInt(validMobs.length - 1);
			currentTaskAmount = mobNumbers[currentTaskID][0] + ModjamMod.r.nextInt(mobNumbers[currentTaskID][1]);
		} else if(currentTask.equals("Burn")) {
			currentTaskID = ModjamMod.r.nextInt(validItems.length);

			if(validItems[currentTaskID].getItem() == Items.nether_star) {currentTaskAmount = 1;}
			else {currentTaskAmount = 5 + ModjamMod.r.nextInt(28);}

			if(validItems[currentTaskID].getItem() instanceof ItemBlock) {currentTaskAmount /= 4;}
		}

		rewardLevels = 5 + ModjamMod.r.nextInt(6);

		markDirty();
	}

	public String getDisadvantage() {
		return ModjamMod.disableDisadvantages ? "None" : currentDisadvantage;
	}
}
