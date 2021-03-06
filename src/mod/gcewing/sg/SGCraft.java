//------------------------------------------------------------------------------------------------
//
//   SG Craft - Main Class
//
//------------------------------------------------------------------------------------------------

package gcewing.sg;

import java.util.*;

import gcewing.sg.rf.RFIntegration;
import net.minecraft.block.*;
import net.minecraft.block.material.*;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.*;
import net.minecraft.world.gen.structure.*;

import net.minecraftforge.common.*;
import net.minecraftforge.event.*;
import net.minecraftforge.event.world.*;
import net.minecraftforge.event.terraingen.*;

import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.gameevent.*;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.common.registry.*;
import static net.minecraftforge.fml.common.registry.VillagerRegistry.*;

// import dan200.computercraft.api.*; //[CC]
import gcewing.sg.cc.*; //[CC]
import gcewing.sg.oc.*; //[OC]

@Mod(modid = Info.modID, name = Info.modName, version = Info.versionNumber,
    acceptableRemoteVersions = Info.versionBounds, dependencies = "after:opencomputers;after:ic2;after:computercraft")

public class SGCraft extends BaseMod<SGCraftClient> {

    public static final Material machineMaterial = new Material(MapColor.IRON);

    public static SGCraft mod;

    public static SGChannel channel;
    public static BaseTEChunkManager chunkManager;
    
    public static SGBaseBlock sgBaseBlock;
    public static SGRingBlock sgRingBlock;
    public static DHDBlock sgControllerBlock;
    //public static SGPortalBlock sgPortalBlock;
    public static Block naquadahBlock, naquadahOre;
    
    public static Item naquadah, naquadahIngot, sgCoreCrystal, sgControllerCrystal, sgChevronUpgrade,
        sgIrisUpgrade, sgIrisBlade;
    
    public static Block ic2PowerUnit;
    public static Item ic2Capacitor;
    public static Block rfPowerUnit;
    
    public static boolean addOresToExistingWorlds;
    public static NaquadahOreWorldGen naquadahOreGenerator;
//     public static int tokraVillagerID;
    
    public static BaseSubsystem ic2Integration; //[IC2]
    public static IIntegration ccIntegration; //[CC]
    public static OCIntegration ocIntegration; //[OC]
    public static RFIntegration rfIntegration; //[RF]
//     public static MystcraftIntegration mystcraftIntegration; //[MYST]

    public SGCraft() {
        mod = this;
        creativeTab = new CreativeTabs("sgcraft:sgcraft") {
            @Override
            public ItemStack getTabIconItem() {
                return new ItemStack(Item.getItemFromBlock(sgBaseBlock));
            }
        };
    }
    
    @Mod.EventHandler
    @Override
    public void preInit(FMLPreInitializationEvent e) {
        FMLCommonHandler.instance().bus().register(this);
        rfIntegration = (RFIntegration) integrateWithMod("forge", "gcewing.sg.rf.RFIntegration"); //[RF]
        ic2Integration = integrateWithMod("ic2", "gcewing.sg.ic2.IC2Integration"); //[IC2]
        ccIntegration = (CCIntegration)integrateWithMod("computercraft", "gcewing.sg.cc.CCIntegration"); //[CC]
        ocIntegration = (OCIntegration)integrateWithMod("opencomputers", "gcewing.sg.oc.OCIntegration"); //[OC]
//         mystcraftIntegration = (MystcraftIntegration)integrateWithMod("Mystcraft", "gcewing.sg.MystcraftIntegration"); //[MYST]
        super.preInit(e);
    }
    
    @Mod.EventHandler
    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
        System.out.printf("SGCraft.init\n");
        configure();
        channel = new SGChannel(Info.modID);
        chunkManager = new BaseTEChunkManager(this);
    }

    @Mod.EventHandler
    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
    }

    @Override   
    protected SGCraftClient initClient() {
        return new SGCraftClient(this);
    }

    @Override
    void configure() {
        DHDTE.configure(config);
        NaquadahOreWorldGen.configure(config);
        SGBaseBlock.configure(config);
        SGBaseTE.configure(config);
        FeatureGeneration.configure(config);
        addOresToExistingWorlds = config.getBoolean("options", "addOresToExistingWorlds", false);
    }       

    @Override
    protected void registerOther() {
        MinecraftForge.TERRAIN_GEN_BUS.register(this);
    }

    @Override
    protected void registerBlocks() {
        sgRingBlock = newBlock("stargateRing", SGRingBlock.class, SGRingItem.class);
        sgBaseBlock = newBlock("stargateBase", SGBaseBlock.class);
        sgControllerBlock = newBlock("stargateController", DHDBlock.class);
        //sgPortalBlock = newBlock("stargatePortal", SGPortalBlock.class);
        naquadahBlock = newBlock("naquadahBlock", NaquadahBlock.class);
        naquadahOre = newBlock("naquadahOre", NaquadahOreBlock.class);
    }
    
    @Override
    protected void registerItems() {
        naquadah = newItem("naquadah"); //, "Naquadah");
        naquadahIngot = newItem("naquadahIngot"); //, "Naquadah Alloy Ingot");
        sgCoreCrystal = newItem("sgCoreCrystal"); //, "Stargate Core Crystal");
        sgControllerCrystal = newItem("sgControllerCrystal"); //, "Stargate Controller Crystal");
        sgChevronUpgrade = addItem(new SGChevronUpgradeItem(), "sgChevronUpgrade");
        sgIrisUpgrade = addItem(new SGIrisUpgradeItem(), "sgIrisUpgrade");
        sgIrisBlade = newItem("sgIrisBlade");
        if (isModLoaded("ic2") || !isModLoaded("thermalexpansion")) {
            ic2Capacitor = newItem("ic2Capacitor");
        }
    }
    
    public static boolean isValidStargateUpgrade(Item item) {
        return item == sgChevronUpgrade || item == sgIrisUpgrade;
    }
    
    @Override
    protected void registerOres() {
        addOre("oreNaquadah", naquadahOre);
        addOre("naquadah", naquadah);
        addOre("ingotNaquadahAlloy", naquadahIngot);
    }

    @Override
    protected void registerRecipes() {
        ItemStack chiselledSandstone = new ItemStack(Blocks.SANDSTONE, 1, 1);
        ItemStack smoothSandstone = new ItemStack(Blocks.SANDSTONE, 1, 2);
        ItemStack sgChevronBlock = new ItemStack(sgRingBlock, 1, 1);
        ItemStack blueDye = new ItemStack(Items.DYE, 1, 4);
        ItemStack orangeDye = new ItemStack(Items.DYE, 1, 14);

        if (config.getBoolean("options", "allowCraftingNaquadah", false))
            newShapelessRecipe("naquada",naquadah, 1, Ingredient.fromItems(Items.COAL, Items.SLIME_BALL, Items.BLAZE_POWDER));
        newRecipe("sgringblock", sgRingBlock, 1, "CCC", "NNN", "SSS",
            'S', smoothSandstone, 'N', "ingotNaquadahAlloy", 'C', chiselledSandstone);
        newRecipe("sgcheveronblock", sgChevronBlock, "CgC", "NpN", "SrS",
            'S', smoothSandstone, 'N', "ingotNaquadahAlloy", 'C', chiselledSandstone,
            'g', Items.GLOWSTONE_DUST, 'r', Items.REDSTONE, 'p', Items.ENDER_PEARL);
        newRecipe("sgbaseblock", sgBaseBlock, 1, "CrC", "NeN", "ScS",
            'S', smoothSandstone, 'N', "ingotNaquadahAlloy", 'C', chiselledSandstone,
            'r', Items.REDSTONE, 'e', Items.ENDER_EYE, 'c', sgCoreCrystal);
        newRecipe("sgcontrollerblock", sgControllerBlock, 1, "bbb", "OpO", "OcO",
            'b', Blocks.STONE_BUTTON, 'O', Blocks.OBSIDIAN, 'p', Items.ENDER_PEARL,
            'c', sgControllerCrystal);
        newShapelessRecipe("naquadahingot",naquadahIngot, 1, Ingredient.fromItem(Items.IRON_INGOT),
                Ingredient.fromItem(naquadah));
        newRecipe("naquadahblock", naquadahBlock, 1, "NNN", "NNN", "NNN", 'N', "ingotNaquadahAlloy");
        newRecipe("sgchevronupgrade", sgChevronUpgrade, 1, "g g", "pNp", "r r",
            'N', "ingotNaquadahAlloy",
            'g', Items.GLOWSTONE_DUST, 'r', Items.REDSTONE, 'p', Items.ENDER_PEARL);
        newRecipe("naquadahingot_from_block", naquadahIngot, 9, "B", 'B', naquadahBlock);
        newRecipe("sgirisblade", sgIrisBlade, 1, " ii", "ic ", "i  ",
            'i', Items.IRON_INGOT, 'c', new ItemStack(Items.COAL, 1, 1));
        newRecipe("sgirisupgrade", sgIrisUpgrade, 1, "bbb", "brb", "bbb",
            'b', sgIrisBlade, 'r', Items.REDSTONE);
        if (config.getBoolean("options", "allowCraftingCrystals", false)) {
            newRecipe("sgcorecrystal", sgCoreCrystal, 1, "bbr", "rdb", "brb",
                'b', blueDye, 'r', Items.REDSTONE, 'd', Items.DIAMOND);
            newRecipe("sgcontrollercrystal", sgControllerCrystal, 1, "roo", "odr", "oor",
                'o', orangeDye, 'r', Items.REDSTONE, 'd', Items.DIAMOND);
        }
        if (!isModLoaded("ic2"))
            addGenericCapacitorRecipe();
    }
    
    protected void addGenericCapacitorRecipe() {
        newRecipe("ic2capacitor", ic2Capacitor, 1, "iii", "ppp", "iii",
            'i', "ingotIron", 'p', "paper");
    }

    @Override
    protected void registerContainers() {
        addContainer(SGGui.SGBase, SGBaseContainer.class);
        addContainer(SGGui.DHDFuel, DHDFuelContainer.class);
        addContainer(SGGui.PowerUnit, PowerContainer.class);
    }
   
    @Override
    protected void registerWorldGenerators() {
        if (config.getBoolean("options", "enableNaquadahOre", true)) {
            System.out.printf("SGCraft: Registering NaquadahOreWorldGen\n");
            naquadahOreGenerator = new NaquadahOreWorldGen();
            GameRegistry.registerWorldGenerator(naquadahOreGenerator, 0);
        }
        MapGenStructureIO.registerStructureComponent(FeatureUnderDesertPyramid.class,
            "SGCraft:FeatureUnderDesertPyramid");
    }
    
    @Override //[VILL]
    protected void registerVillagers() {
        VillagerProfession tokraProfession = new VillagerProfession("sgcraft:tokra", "sgcraft:textures/skins/tokra.png","sgcraft:textures/skins/tokra.png");
        // Update: Needs new skin for Zombie mode?
        VillagerCareer tokraCareer = new VillagerCareer(tokraProfession, "sgcraft:tokra");
        tokraCareer.addTrade(1, new SGTradeHandler());
        ForgeRegistries.VILLAGER_PROFESSIONS.register(tokraProfession);
    }

    @Override
    protected void registerEntities() {
        addEntity(IrisEntity.class, "Stargate Iris", SGEntity.Iris, 1000000, false);
    }
    
    @Override
    protected void registerSounds() {
        SGBaseTE.registerSounds(this);
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkDataEvent.Load e) {
        Chunk chunk = e.getChunk();
        SGChunkData.onChunkLoad(e);
    }

    @SubscribeEvent
    public void onChunkSave(ChunkDataEvent.Save e) {
        Chunk chunk = e.getChunk();
        SGChunkData.onChunkSave(e);
    }
    
    @SubscribeEvent
    public void onInitMapGen(InitMapGenEvent e) {
        FeatureGeneration.onInitMapGen(e);
    }
    
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent e) {
        switch (e.phase) {
            case START: {
                for (BaseSubsystem om : subsystems)
                    if (om instanceof IIntegration)
                        ((IIntegration)om).onServerTick();
                break;
            }
        }
    }
    
    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload e) {
        Chunk chunk = e.getChunk();
        if (!chunk.getWorld().isRemote) {
            for (Object obj : chunk.getTileEntityMap().values()) {
                if (obj instanceof SGBaseTE) {
                    SGBaseTE te = (SGBaseTE)obj;
                    te.disconnect();
                }
            }
        }
    }

}
