package com.examplemod;

import static com.mumfrey.liteloader.gl.GL.GL_GREATER;
import static com.mumfrey.liteloader.gl.GL.GL_ONE_MINUS_SRC_ALPHA;
import static com.mumfrey.liteloader.gl.GL.GL_SRC_ALPHA;
import static com.mumfrey.liteloader.gl.GL.glAlphaFunc;
import static com.mumfrey.liteloader.gl.GL.glBlendFunc;
import static com.mumfrey.liteloader.gl.GL.glColor4f;
import static com.mumfrey.liteloader.gl.GL.glDisableBlend;
import static com.mumfrey.liteloader.gl.GL.glDisableCulling;
import static com.mumfrey.liteloader.gl.GL.glDisableLighting;
import static com.mumfrey.liteloader.gl.GL.glDisableTexture2D;
import static com.mumfrey.liteloader.gl.GL.glEnableCulling;
import static com.mumfrey.liteloader.gl.GL.glEnableTexture2D;

import java.awt.Color;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.ReadableColor;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mumfrey.liteloader.Tickable;
import com.mumfrey.liteloader.core.LiteLoader;
import com.mumfrey.liteloader.modconfig.ConfigStrategy;
import com.mumfrey.liteloader.modconfig.ExposableOptions;

@ExposableOptions(strategy = ConfigStrategy.Versioned, filename="examplemod.json")
public class LiteModExample implements Tickable
{
	/**
	 * This is a keybinding that we will register with the game and use to toggle the clock
	 * 
	 * Notice that we specify the key name as an *unlocalised* string. The localisation is provided from the included resource file
	 */
	//private static KeyBinding testKeyBinding = new KeyBinding("key.test", Keyboard.KEY_F4, "key.categories.litemods");

	/**
	 * Default constructor. All LiteMods must have a default constructor. In general you should do very little
	 * in the mod constructor EXCEPT for initializing any non-game-interfacing components or performing
	 * sanity checking prior to initialization
	 */
	public LiteModExample(){}
	
	/**
	 * getName() should be used to return the display name of your mod and MUST NOT return null
	 * 
	 * @see com.mumfrey.liteloader.LiteMod#getName()
	 */
	@Override
	public String getName() {return "Armor Mod";}
	
	/**
	 * getVersion() should return the same version string present in the mod metadata, although this is
	 * not a strict requirement.
	 * 
	 * @see com.mumfrey.liteloader.LiteMod#getVersion()
	 */
	@Override
	public String getVersion() {return "1.1";}
	
	/**
	 * init() is called very early in the initialization cycle, before the game is fully initialized, this
	 * means that it is important that your mod does not interact with the game in any way at this point.
	 * 
	 * @see com.mumfrey.liteloader.LiteMod#init(java.io.File)
	 */
	@Override
	public void init(File configPath)
	{
		// The key binding declared above won't do anything unless we register it, ModUtilties provides 
		// a convenience method for this
		
		//LiteLoader.getInput().registerKeyBinding(LiteModExample.testKeyBinding);
	}
	
	/**
	 * upgradeSettings is used to notify a mod that its version-specific settings are being migrated
	 * 
	 * @see com.mumfrey.liteloader.LiteMod#upgradeSettings(java.lang.String, java.io.File, java.io.File)
	 */
	@Override
	public void upgradeSettings(String version, File configPath, File oldConfigPath){}
	
	
	
	
	
	private long time = System.nanoTime();
	private Random r = new Random();

	private boolean opened = false;
	private Minecraft minecraft;
	
	@Override
	public void onTick(Minecraft minecraft, float partialTicks, boolean inGame, boolean clock)
	{
		this.minecraft = minecraft;
		// The three checks here are critical to ensure that we only draw the clock as part of the "HUD"
		// and don't draw it over active GUI's or other elements
		GuiScreen screen = minecraft.currentScreen;
		
		if (inGame && screen == null/* && Minecraft.isGuiEnabled()*/)
		{
			//if (LiteModExample.testKeyBinding.isPressed()) //F4
			//{
				
			//}
			
			
		}
		else if (screen instanceof GuiInventory){

			for (ItemStack stack : getPlayerArmorInventory())
				if (stack != null && stack.getItem() instanceof ItemArmor)
					addArmorLore(stack, Colors.BLUE, false);
			
			for (ItemStack stack : getPlayerMainInventory())
				if (stack != null && stack.getItem() instanceof ItemArmor)
					compareToCurrentArmor(stack);
			
		} 
		else if (screen instanceof GuiChest){
			
			GuiChest chest = ((GuiChest) screen);
			Container chestInv = chest.inventorySlots;
			for (Object s : chestInv.inventorySlots){
				ItemStack stack = ((Slot) s).getStack();
				if (stack != null/* && stack.getItem() instanceof ItemArmor*/)
					compareToCurrentArmor(stack);
			}
		}
		else {
			//say(minecraft.currentScreen);
		}
		
	}
	
	/**
	 * Detects what type of Armor item is and compare it to the current
	 * @param item Armor to compare to the current
	 */
	
	private void compareToCurrentArmor(ItemStack item){
		//Helmet = 3
		//Boots = 0;
		if (item != null){
			if (isHelmet(item))
				compareArmor(getCurrentPlayerHelmet(), item);
			if (isChestplate(item))
				compareArmor(getCurrentPlayerChest(), item);
			if (isPants(item))
				compareArmor(getCurrentPlayerPants(), item);
			if (isBoots(item))
				compareArmor(getCurrentPlayerBoots(), item);
		}
	}
	
	private void compareArmor(ItemStack current, ItemStack other){
		if (current != null && other != null && current.getItem() instanceof ItemArmor && other.getItem() instanceof ItemArmor){
			if ((isHelmet(current) && isHelmet(other)) || (isChestplate(current) && isChestplate(other)) || (isPants(current) && isPants(other)) || (isBoots(current) && isBoots(other))){
				if (getDamageReduction(current) > getDamageReduction(other)){
					addArmorLore(other, Colors.RED, true);
				}
				else if (getDamageReduction(current) == getDamageReduction(other)){
					addArmorLore(other, Colors.BLUE, false);
				}
				else{
					addArmorLore(other, Colors.GREEN, true);
				}
			}
		}
		else if (current == null && other != null && other.getItem() instanceof ItemArmor){
			addArmorLore(other, Colors.GREEN, true);
		}
		else if (current != null && other != null && other.getItem() instanceof ItemArmor){
			addArmorLore(other, Colors.GREEN, true);
		}
	}
	
	/**
	 * @return Item on head, can return null or a non-ItemArmor 
	 */
	
	private ItemStack getCurrentPlayerHelmet(){
		return getPlayerArmorInventory()[3];
	}
	
	/**
	 * @return Item on chest, can return null or a non-ItemArmor 
	 */
	
	private ItemStack getCurrentPlayerChest(){
		return getPlayerArmorInventory()[2];
	}
	
	/**
	 * @return Item on legs, can return null or a non-ItemArmor 
	 */
	
	private ItemStack getCurrentPlayerPants(){
		return getPlayerArmorInventory()[1];
	}
	
	/**
	 * @return Item on feet, can return null or a non-ItemArmor 
	 */
	
	private ItemStack getCurrentPlayerBoots(){
		return getPlayerArmorInventory()[0];
	}
	
	/**
	 * Adds preset lore to armor
	 * @param armor the ItemStack to add lore to
	 * @param color Color of the lore text
	 * @param showCurrent whether to show the current damage reduction
	 */
	
	private void addArmorLore(ItemStack armor, Colors color, boolean showCurrent){
		
		String currentReduction ="0";
		if (armor != null){
			if (isHelmet(armor))
				currentReduction = getDamageReductionRange(getCurrentPlayerHelmet());
			if (isChestplate(armor))
				currentReduction = getDamageReductionRange(getCurrentPlayerChest());
			if (isPants(armor))
				currentReduction = getDamageReductionRange(getCurrentPlayerPants());
			if (isBoots(armor))
				currentReduction = getDamageReductionRange(getCurrentPlayerBoots());
		}
		
		if (showCurrent){
			addLore(armor, color.getColor() + "Damage Reduction: " + getDamageReductionRange(armor) + "%", Colors.BLUE.getColor() + "Current: " +  currentReduction + "%");
		}
		else{
			addLore(armor, color.getColor() + "Damage Reduction: " + getDamageReductionRange(armor) + "%");
		}
		
		
	}
	
//	private String getTotalReduction(dunnoWhatGoesHere){
//	sum all epf values, cap at 25
//	multiply by .5 and 1 for bounds, cap at 20
//	int dmgLeft = 100 - allArmorReduction;
//	lowB = dmgLeft * (lowEpf/100);
//	highB = dmgLeft * (highEpf/100);
	
	/**
	 * Gets damage reduction as a range
	 * @param stack Item to get range of
	 * @return String of damage reduction(s)
	 */
	
	private String getDamageReductionRange(ItemStack stack){
		if (stack == null || !(stack.getItem() instanceof ItemArmor))
			return "0";
		if (!stack.isItemEnchanted())
			return getDamageReduction(stack) + "";
		
		ItemArmor armor = ((ItemArmor) stack.getItem());
		
		int level = 0;
		
		for (NBTTagCompound tag : getEnchantsTagArray(stack))
			if (stack.isItemEnchanted())
				if(getEnchantID(tag) == Enchantment.protection.effectId)
					level = getEnchantLevel(tag);
		
		if (level == 0){
			return armor.damageReduceAmount * 4 + "";
		}
		else{
			return ((armor.damageReduceAmount * 4 + (int) Math.floor((6 + Math.pow(level, 2)) * TypeModifier.protection.getLowerValue() / 3 )) + "-" + (armor.damageReduceAmount * 4 + (int) Math.floor((6 + Math.pow(level, 2)) * TypeModifier.protection.getUpperValue() / 3 )));
		}
		
	}
	
	/**
	 * Returns averaged damage reduction
	 * @param stack item to get reduction of
	 * @return int of amount of damage reduction
	 */
	
	private int getDamageReduction(ItemStack stack){
		if (stack == null)
			return 0;
		ItemArmor armor = ((ItemArmor) stack.getItem());
		
		int level = 0;
		
		for (NBTTagCompound tag : getEnchantsTagArray(stack))
			if (stack.isItemEnchanted())
				if(getEnchantID(tag) == Enchantment.protection.effectId)
					level = getEnchantLevel(tag);
		
		if (level == 0){
			return armor.damageReduceAmount * 4;
		}
		else{
			return (armor.damageReduceAmount * 4 + (int) Math.floor((6 + Math.pow(level, 2)) * TypeModifier.protection.getAverageValue() / 3 ));
		}
		
		//floor ( (6 + pow(level,2)) * TypeModifier.protection.getAverageValue() / 3 )
		
		
		//double epfHelm = Math.floor( (6 + level^2) * .75 / 3 );
				//double epfChest = Math.floor( (6 + level^2) * .75 / 3 );
				//double epfPant = Math.floor( (6 + level^2) * .75 / 3 );
				//double epfBoot = Math.floor( (6 + level^2) * .75 / 3 );
				//double epfTot = epfHelm + epfChest + epfPant + epfBoot;
		//if (epfTot > 25)
		//	epfTot = 25;
		//double lowBProt = Math.ceil(epfTot/2);
		//double upBProt = Math.ceil(epfTot);
		//if (lowBProt > 20)
		//	lowBProt = 20;
		//if (upBProt > 20)
		//	upBProt = 20;
		
		//lower and upper bound of the protection %, add to armor's damage reduction %
		//lowBProt = lowBProt * 4;
		//upBProt = upBProt * 4;
		
	}
	
	/**
	 * Add Lore to an Item
	 * @param stack ItemStack to add lore to
	 * @param strings Each is a seperate line of lore
	 */
	
	public void addLore(ItemStack stack, String... strings){
		
		if (stack.getTagCompound() == null)
        {
			stack.setTagCompound(new NBTTagCompound());
        }
		NBTTagCompound compound = stack.getTagCompound();
		if (!compound.hasKey("display")){
			compound.setTag("display", new NBTTagCompound());
		}
		NBTTagCompound display = compound.getCompoundTag("display");
		display.setTag("Lore", new  NBTTagList());
		NBTTagList list = display.getTagList("Lore", 0);

		for (String s : strings)
			list.appendTag(new NBTTagString(s));
	}
	
	private boolean isHelmet(ItemStack stack){
		return isHelmet(stack.getItem());
	}
	
	private boolean isHelmet(Item item){
		return item instanceof ItemArmor && item.getUnlocalizedName().contains("helmet");
	}
	
	private boolean isHelmet(ItemArmor armor){
		return armor.getUnlocalizedName().contains("helmet");
	}
	
	private boolean isChestplate(ItemStack stack){
		return isChestplate(stack.getItem());
	}
	
	private boolean isChestplate(Item item){
		return item instanceof ItemArmor && item.getUnlocalizedName().contains("chestplate");
	}
	
	private boolean isChestplate(ItemArmor armor){
		return armor.getUnlocalizedName().contains("chestplate");
	}
	
	private boolean isPants(ItemStack stack){
		return isPants(stack.getItem());
	}
	
	private boolean isPants(Item item){
		return item instanceof ItemArmor && item.getUnlocalizedName().contains("leggings");
	}
	
	private boolean isPants(ItemArmor armor){
		return armor.getUnlocalizedName().contains("leggings");
	}
	
	private boolean isBoots(ItemStack stack){
		return isBoots(stack.getItem());
	}
	
	private boolean isBoots(Item item){
		return item instanceof ItemArmor && item.getUnlocalizedName().contains("boots");
	}
	
	private boolean isBoots(ItemArmor armor){
		return armor.getUnlocalizedName().contains("boots");
	}
	
	
	
	private NBTTagList getEnchantsTagList(ItemStack stack){
		return stack.getEnchantmentTagList();
	}
	
	private NBTTagCompound[] getEnchantsTagArray(ItemStack stack){
		
		if (stack.isItemEnchanted()){
			NBTTagList tags = getEnchantsTagList(stack);
			NBTTagCompound[] enchants = new NBTTagCompound[tags.tagCount()];
			
			for (int i=0; i < tags.tagCount(); i++){
				enchants[i] = tags.getCompoundTagAt(i);
			}
			return enchants;
		}
		else{
			return new NBTTagCompound[0];
		}
	}
	
	private int getEnchantLevel(NBTTagCompound enchantTag){
		NBTBase id = enchantTag.getTag("lvl");
		if (id != null)
			return Integer.parseInt(id.toString().substring(0, id.toString().length()-1));
		else
			return -1;
	}
	
	private int getEnchantID(NBTTagCompound enchantTag){
		NBTBase id = enchantTag.getTag("id");
		if (id != null)
			return Integer.parseInt(id.toString().substring(0, id.toString().length()-1));
		else
			return -1;
	}
	
	private void getHotbarItems(){
		Arrays.copyOfRange(getPlayerMainInventory(), 0, 8);
	}
	
	private ItemStack getItemInHand(){
		return getPlayerMainInventory()[getItemInHandSlotNumber()];
	}
	
	private int getItemInHandSlotNumber(){
		return minecraft.thePlayer.inventory.currentItem;
	}
	
	private ItemStack[] getPlayerMainInventory(){
		return minecraft.thePlayer.inventory.mainInventory;
	}
	
	private ItemStack[] getPlayerArmorInventory(){
		return minecraft.thePlayer.inventory.armorInventory;
	}
	
	
	
	public void addDye(ItemStack stack, int color){
		stack.getTagCompound().getCompoundTag("display").setTag("color", new NBTTagInt(color));
	}
	
	public static void say(Object s){
		System.out.println(s);
	}
	
	
	
	
	
	
	
	
	//Not actually used, but for reference:
	
	
	
	private void enchantTagTester(){
		ItemStack hand = getItemInHand();
		say(hand);
		if (hand.isItemEnchanted()){
			say(getEnchantsTagList(hand));
			say(Arrays.toString(getEnchantsTagArray(hand)));
			for (NBTTagCompound i : getEnchantsTagArray(hand))
				say("id: " + getEnchantID(i) + " lvl: " + getEnchantLevel(i));
		}
	}
	
	/**
	 * Adds sharp 2 to item in hand
	 */
	public void addEnchant(){
		minecraft.thePlayer.inventory.mainInventory[minecraft.thePlayer.inventory.currentItem].addEnchantment(Enchantment.sharpness, 2);
	}
	
	public void addDefaultLore(ItemStack stack){
		addLore(stack, "§d" + "Durability: " + (stack.getMaxDamage()-stack.getItemDamage() + "/" + stack.getMaxDamage()),
				"§a" +"Stackable: " + stack.isStackable(),
				"§b" +"Repair Cost: " + stack.getRepairCost());
	}

	private void addAllDefaultLore(){
		for (ItemStack stack : getPlayerMainInventory()) {
			if (stack != null)
				addDefaultLore(stack);
		}
	
		for (ItemStack stack : getPlayerArmorInventory()) {
			if (stack != null)
				addDefaultLore(stack);
		}
	}

	private void randomDirection(){
		//Spin player in random direction
		if (System.nanoTime() - time > 1000000000){
			minecraft.thePlayer.rotationYaw = /*180*/r.nextInt(360);
			System.out.println(minecraft.thePlayer.rotationYaw);
			time = System.nanoTime();
		}
	}
	
	private void printInventory(){
		//Print out user inventory
		//System.out.println(minecraft.thePlayer.inventory.currentItem);
		System.out.println(minecraft.thePlayer.inventory.mainInventory);
		for (ItemStack stack :minecraft.thePlayer.inventory.mainInventory){
			if (stack != null)
				System.out.println(stack.getDisplayName() + (stack.isStackable()? "(" + stack.stackSize + ") " : " ") + (stack.isItemStackDamageable() ? (stack.getMaxDamage() - stack.getItemDamage()) + "/" + stack.getMaxDamage() : ""));
		}
		
		//Print out user armor inventory
		System.out.println(minecraft.thePlayer.inventory.armorInventory);
		for (ItemStack stack :minecraft.thePlayer.inventory.armorInventory){
			//stack.
			if (stack != null)
				System.out.println(stack.getDisplayName() + (stack.isStackable()? "(" + stack.stackSize + ") " : " ") + (stack.isItemStackDamageable() ? (stack.getMaxDamage() - stack.getItemDamage()) + "/" + stack.getMaxDamage() : ""));
		}
	}
}
