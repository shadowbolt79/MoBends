package net.shadowking97.vivecraftcompat;

import net.gobbob.mobends.util.BendsLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Level;

public class ClientReflector extends VivecraftReflector {

    private boolean enabled = false;

    private Minecraft mc = Minecraft.getMinecraft();

    private Class<?> cNetworkHelper;
    private Class<?> cVivePlayer;
    private Class<?> cOpenVRPlayer;
    private Class<?> cVRData;
    private Class<?> cVRDevicePose;

    private Field fVivePlayers;

    private Field fVRPlayer;
    private Field fVRdataWorldPost;

    private Method mGetVivePlayer;
    private Method mContainsVivePlayer;

    private Method mGetPosition;
    private Method mGetRotation;
    private Method mGetController;

    private Field fHMD;

    private Method mGetControllerDir;
    private Method mGetControllerPos;
    private Method mGetHMDDir;
    private Method mGetHMDPos;
    private Method isVR;

    @Override
    public boolean init() {
        enabled = true;
        try {
            cNetworkHelper = Class.forName("com.mtbs3d.minecrift.api.NetworkHelper");
            cVivePlayer = Class.forName("com.mtbs3d.minecrift.api.VivePlayer");

            fVivePlayers = cNetworkHelper.getDeclaredField("vivePlayers");

            mGetVivePlayer = fVivePlayers.getClass().getMethod("get",Object.class);
            mContainsVivePlayer = fVivePlayers.getClass().getMethod("containsKey",Object.class);

            mGetControllerDir = cVivePlayer.getDeclaredMethod("getControllerDir",int.class);
            mGetHMDDir = cVivePlayer.getDeclaredMethod("getHMDDir");
            mGetControllerPos = cVivePlayer.getDeclaredMethod("getControllerPos",int.class);
            mGetHMDPos = cVivePlayer.getDeclaredMethod("getHMDPos");
            isVR = cVivePlayer.getMethod("isVR");

            BendsLogger.log(Level.INFO,"Vivecraft Client detected! Enabling compatibility features.");

        }catch (Exception e){
            enabled = false;
        }

        if(enabled) {
            try { //Unlikely to exist on Non-vr vivecraft client
                cOpenVRPlayer = Class.forName("com.mtbs3d.minecrift.gameplay.OpenVRPlayer");
                cVRData = Class.forName("com.mtbs3d.minecrift.api.VRData");
                cVRDevicePose = Class.forName("com.mtbs3d.minecrift.api.VRDevicePose");

                fVRPlayer = Minecraft.class.getDeclaredField("vrPlayer");
                fVRdataWorldPost = cOpenVRPlayer.getDeclaredField("vrdata_world_post");

                mGetController = cVRData.getMethod("getController",int.class);
                fHMD = cVRData.getDeclaredField("hmd");

                mGetRotation = cVRDevicePose.getMethod("getRotation");
                mGetPosition = cVRDevicePose.getMethod("getPosition");
            } catch (Exception e) {
                fVRPlayer = null;
                BendsLogger.log(Level.INFO,"Vivecraft Non-VR Client");
            }
        }

        return enabled;
    }

    @Override
    public boolean isVRPlayer(EntityPlayer player) {
        if(enabled) {
            try {
                UUID uuid = player.getUniqueID();
                if (uuid.equals(mc.player.getUniqueID())) {
                    return (fVRPlayer != null && fVRPlayer.get(mc) != null);
                } else {
                    Object vivePlayers = fVivePlayers.get(null);
                    if ((boolean) mContainsVivePlayer.invoke(vivePlayers, uuid)) {
                        Object vivePlayer = mGetVivePlayer.invoke(vivePlayers, uuid);
                        return (boolean) isVR.invoke(vivePlayer);
                    }
                }
            } catch (Exception e) {
                BendsLogger.log(Level.SEVERE, "Vivecraft Client: Unknown Error Parsing isVRPlayer\n" + e.getStackTrace());
            }
        }

        return false;
    }

    @Override
    public Vec3d getHMDPos(EntityPlayer player) {
        if(enabled) {
            try {
                UUID uuid = player.getUniqueID();
                if(uuid.equals(mc.player.getUniqueID()))
                {
                    //Single player character - attempt to get from WorldPost data
                    if(fVRPlayer!=null)
                    {
                        Object vrPlayer = fVRPlayer.get(mc);
                        Object vrDataWorldPost = fVRdataWorldPost.get(vrPlayer);
                        Object hmd = fHMD.get(vrDataWorldPost);

                        if(hmd!=null)
                        {
                            return (Vec3d)mGetPosition.invoke(hmd);
                        }
                    }
                }
                else {
                    //Network Character - attempt to get from NetworkHelper
                    Object vivePlayers = fVivePlayers.get(null);
                    if ((boolean) mContainsVivePlayer.invoke(vivePlayers, uuid)) {
                        Object vivePlayer = mGetVivePlayer.invoke(vivePlayers, uuid);
                        return (Vec3d) mGetHMDPos.invoke(vivePlayer);
                    }
                }
            } catch (Exception e) {
                BendsLogger.log(Level.SEVERE, "Vivecraft Client: Unknown Error Parsing getHMDPos\n" + e.getStackTrace());
            }
        }
        return player.getPositionVector().addVector(0, 1.62, 0);
    }

    @Override
    public Vec3d getHMDRot(EntityPlayer player) {
        if(enabled) {
            try {
                UUID uuid = player.getUniqueID();
                if(uuid.equals(mc.player.getUniqueID()))
                {
                    //Single player character - attempt to get from WorldPost data
                    if(fVRPlayer!=null)
                    {
                        Object vrPlayer = fVRPlayer.get(mc);
                        Object vrDataWorldPost = fVRdataWorldPost.get(vrPlayer);
                        Object hmd = fHMD.get(vrDataWorldPost);

                        if(hmd!=null)
                        {
                            return (Vec3d)mGetRotation.invoke(hmd);
                        }
                    }
                }
                else {
                    //Network Character - attempt to get from NetworkHelper
                    Object vivePlayers = fVivePlayers.get(null);
                    if ((boolean) mContainsVivePlayer.invoke(vivePlayers, uuid)) {
                        Object vivePlayer = mGetVivePlayer.invoke(vivePlayers, uuid);
                        return (Vec3d) mGetHMDDir.invoke(vivePlayer);
                    }
                }
            } catch (Exception e) {
                BendsLogger.log(Level.SEVERE, "Vivecraft Client: Unknown Error Parsing getHMDRot\n" + e.getStackTrace());
            }
        }
        return player.getLookVec();
    }

    @Override
    public Vec3d getControllerPos(EntityPlayer player, int c) {
        if(enabled) {
            try {
                UUID uuid = player.getUniqueID();
                if(uuid.equals(mc.player.getUniqueID()))
                {
                    //Single player character - attempt to get from WorldPost data
                    if(fVRPlayer!=null)
                    {
                        Object vrPlayer = fVRPlayer.get(mc);
                        Object vrDataWorldPost = fVRdataWorldPost.get(vrPlayer);
                        Object controller = mGetController.invoke(vrDataWorldPost,c);

                        if(controller!=null)
                        {
                            return (Vec3d)mGetPosition.invoke(controller);
                        }
                    }
                }
                else {
                    //Network Character - attempt to get from NetworkHelper
                    Object vivePlayers = fVivePlayers.get(null);
                    if ((boolean) mContainsVivePlayer.invoke(vivePlayers, uuid)) {
                        Object vivePlayer = mGetVivePlayer.invoke(vivePlayers, uuid);
                        return (Vec3d) mGetControllerPos.invoke(vivePlayer,c);
                    }
                }
            } catch (Exception e) {
                BendsLogger.log(Level.SEVERE, "Vivecraft Client: Unknown Error Parsing getControllerPos\n" + e.getStackTrace());
            }
        }
        return player.getPositionVector().addVector(0, 1.62, 0);
    }

    @Override
    public Vec3d getControllerRot(EntityPlayer player, int c) {
        if(enabled) {
            try {
                UUID uuid = player.getUniqueID();
                if(uuid.equals(mc.player.getUniqueID()))
                {
                    //Single player character - attempt to get from WorldPost data
                    if(fVRPlayer!=null)
                    {
                        Object vrPlayer = fVRPlayer.get(mc);
                        Object vrDataWorldPost = fVRdataWorldPost.get(vrPlayer);
                        Object controller = mGetController.invoke(vrDataWorldPost,c);

                        if(controller!=null)
                        {
                            return (Vec3d)mGetRotation.invoke(controller);
                        }
                    }
                }
                else {
                    //Network Character - attempt to get from NetworkHelper
                    Object vivePlayers = fVivePlayers.get(null);
                    if ((boolean) mContainsVivePlayer.invoke(vivePlayers, uuid)) {
                        Object vivePlayer = mGetVivePlayer.invoke(vivePlayers, uuid);
                        return (Vec3d) mGetControllerDir.invoke(vivePlayer,c);
                    }
                }
            } catch (Exception e) {
                BendsLogger.log(Level.SEVERE, "Vivecraft Client: Unknown Error Parsing getHMDPos\n" + e.getStackTrace());
            }
        }
        return player.getLookVec();
    }
}
