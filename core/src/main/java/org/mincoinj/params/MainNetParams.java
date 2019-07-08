/*
 * Copyright 2013 Google Inc.
 * Copyright 2015 Andreas Schildbach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mincoinj.params;

import org.mincoinj.core.*;
import org.mincoinj.net.discovery.*;

import java.net.*;

import static com.google.common.base.Preconditions.*;

/**
 * Parameters for the main production network on which people trade goods and services.
 */
public class MainNetParams extends AbstractBitcoinNetParams {
    public static final int MAINNET_MAJORITY_WINDOW = 10000;
    public static final int MAINNET_MAJORITY_REJECT_BLOCK_OUTDATED = 9500;
    public static final int MAINNET_MAJORITY_ENFORCE_BLOCK_UPGRADE = 7500;

    public MainNetParams() {
        super();
        interval = INTERVAL;
        targetTimespan = TARGET_TIMESPAN;
        //maxTarget = Utils.decodeCompactBits(0x1e0ffff0L);
        maxTarget = Utils.decodeCompactBits(0x1f00ffffL);
        dumpedPrivateKeyHeader = 178;
        addressHeader = 50;
        p2shHeader = 5;
        segwitAddressHrp = "bc";
        port = 9334;
        packetMagic = 0x6342212cL;
        bip32HeaderP2PKHpub = 0x0488b21e; // The 4 byte header that serializes in base58 to "xpub".
        bip32HeaderP2PKHpriv = 0x0488ade4; // The 4 byte header that serializes in base58 to "xprv"
        bip32HeaderP2WPKHpub = 0x04b24746; // The 4 byte header that serializes in base58 to "zpub".
        bip32HeaderP2WPKHpriv = 0x04b2430c; // The 4 byte header that serializes in base58 to "zprv"

        majorityEnforceBlockUpgrade = MAINNET_MAJORITY_ENFORCE_BLOCK_UPGRADE;
        majorityRejectBlockOutdated = MAINNET_MAJORITY_REJECT_BLOCK_OUTDATED;
        majorityWindow = MAINNET_MAJORITY_WINDOW;

        genesisBlock.setDifficultyTarget(0x1e0ffff0L);
        genesisBlock.setTime(1317972665);
        genesisBlock.setNonce(2084524493);
        id = ID_MAINNET;
        subsidyDecreaseBlockCount = 1440;  /* cryptodad May 2019 - mincoin mainnet */
        spendableCoinbaseDepth = 300;
        String genesisHash = genesisBlock.getHashAsString();
        checkState(genesisHash.equals("12a765e31ffd4059bada1e25190f6e98c99d9714d334efa41a195a7e7e04bfe2"),
                genesisHash);

        // This contains (at a minimum) the blocks which are not BIP30 compliant. BIP30 changed how duplicate
        // transactions are handled. Duplicated transactions could occur in the case where a coinbase had the same
        // extraNonce and the same outputs but appeared at different heights, and greatly complicated re-org handling.
        // Having these here simplifies block connection logic considerably.
        checkpoints.put(234665, Sha256Hash.wrap("fba8afe9fc734e146a2273cb956d2c30c7a86832007ded99d6b5fd2637d718e3"));
        checkpoints.put(1438440, Sha256Hash.wrap("fdb38e23fda036ef965f270285d9a6dd2ce8a05d7c2f3dcd5323d9c834d14799"));
        checkpoints.put(2029907, Sha256Hash.wrap("67fa9341f35b8bf1170780322bc977cecf946b703021bd2366984b83831dbb82"));
        checkpoints.put(2215946, Sha256Hash.wrap("e5cbf406c007ebbd2d8ff2cee464b48523f013cbdc4e35b22294b4eef59123f7"));

        dnsSeeds = new String[] {
                "seed.mincointools.com",
                "seed.mincoinpool.org"
        };

        /* cryptodad Jun 2019 */
        /*
        httpSeeds = new HttpDiscovery.Details[] {
                // Andreas Schildbach
                new HttpDiscovery.Details(
                        ECKey.fromPublicOnly(Utils.HEX.decode("0238746c59d46d5408bf8b1d0af5740fe1a6e1703fcb56b2953f0b965c740d256f")),
                        URI.create("http://httpseed.bitcoin.schildbach.de/peers")
                )
        };
        */
        httpSeeds = null;
        /* cryptodad Jun 2019 end */

        addrSeeds = new int[] {
                0x00000000, 0x00000000, 0x0000ffff, 0x17fdcd86,
                0x00000000, 0x00000000, 0x0000ffff, 0x2d4de626,
                0x00000000, 0x00000000, 0x0000ffff, 0x4e81efb3,
                0x00000000, 0x00000000, 0x0000ffff, 0x50d0e798,
                0x00000000, 0x00000000, 0x0000ffff, 0x6882c937,
                0x00000000, 0x00000000, 0x0000ffff, 0x6882d3df,
                0x00000000, 0x00000000, 0x0000ffff, 0x77096c73,
                0x00000000, 0x00000000, 0x0000ffff, 0x77096c7d,
                0x00000000, 0x00000000, 0x0000ffff, 0xae04ded0,
                0x2a027b40, 0x50d0e798, 0x00000000, 0x00000001,
                0xfd87d87e, 0xeb432482, 0x4f7812b2, 0x250618da,
                0xfd87d87e, 0xeb432db6, 0xfbfdd050, 0xb91ff626,
                0xfd87d87e, 0xeb436a93, 0xb9f4d1e6, 0xb5b9122a,
                0xfd87d87e, 0xeb43ad2a, 0x564863a7, 0xb2b59894
        };
    }

    private static MainNetParams instance;

    public static synchronized MainNetParams get() {
        if (instance == null) {
            instance = new MainNetParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return PAYMENT_PROTOCOL_ID_MAINNET;
    }
}
