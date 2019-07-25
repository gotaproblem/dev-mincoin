/*
 * Copyright 2013 Google Inc.
 * Copyright 2014 Andreas Schildbach
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

import java.math.BigInteger;
import java.util.Date;

import org.mincoinj.core.Block;
import org.mincoinj.core.NetworkParameters;
import org.mincoinj.core.Sha256Hash;
import org.mincoinj.core.StoredBlock;
import org.mincoinj.core.Utils;
import org.mincoinj.core.VerificationException;
import org.mincoinj.store.BlockStore;
import org.mincoinj.store.BlockStoreException;

import static com.google.common.base.Preconditions.checkState;

/**
 * Parameters for the testnet4, a separate public instance of Mincoin that has relaxed rules suitable for development
 * and testing of applications and new Mincoin versions.
 */
public class TestNet4Params extends AbstractBitcoinNetParams {
    public static final int TESTNET_MAJORITY_WINDOW = 10000;
    public static final int TESTNET_MAJORITY_REJECT_BLOCK_OUTDATED = 7500;
    public static final int TESTNET_MAJORITY_ENFORCE_BLOCK_UPGRADE = 5100;

    public TestNet4Params() {
        super();
        id = ID_TESTNET;
        packetMagic = 0x8080d8e9L;
        interval = INTERVAL;
        targetTimespan = TARGET_TIMESPAN;
        maxTarget = Utils.decodeCompactBits(0x1f00ffffL);
        port = 19334;
        addressHeader = 111;
        p2shHeader = 196;
        dumpedPrivateKeyHeader = 239;
        segwitAddressHrp = "tb";                /* cryptodad Jul 2019 - this requires bech32 which isn't implemented until bitcoin 0.16 */
        genesisBlock.setTime(1550577517L);
        genesisBlock.setDifficultyTarget(0x1f00ffffL);
        genesisBlock.setNonce(385474691);
        spendableCoinbaseDepth = 300;

        /* cryptodad Jun 2019 - testnet4

           0    - 1439 reward = 500
           1440 - 2879 reward = 100
           2880 - 4319 reward = 50
           4320 - 5759 reward = 2
         */
        subsidyDecreaseBlockCount = 1440; /* cryptodad May 2019 - mincoin testnet4 */
        String genesisHash = genesisBlock.getHashAsString();
        checkState(genesisHash.equals("195b2e22e075bcd8bf4379b872a279e8974a066a6b3c56bb3f2b20c22b3c3721"));
        alertSigningKey = Utils.HEX.decode("04302390343f91cc401d56d68b123028bf52e5fca1939df127f63c6467cdf9c8e2c14b61104cf817d0b780da337893ecc4aaff1309e536162dabbdb45200ca2b0a");

        httpSeeds = null;

        dnsSeeds = new String[] {
                "testnet-seed.mincointools.com",
                "testnet-seed.mincoinpool.org"
        };

        /* cryptodad May 2019 */
       // checkpoints.put(122573, Sha256Hash.wrap("b0e5151fbbe4fd6d6b0007d682af8b65f1c6c7d417b6f3b90fcc77e6f68aeb39"));

        addrSeeds = new int[] {
                0x00000000,0x00000000,0x0000ffff,0x17fdcd86,
                0x00000000,0x00000000,0x0000ffff,0x6882d3df,
                0x00000000,0x00000000,0x0000ffff,0x77096c73,
                0x00000000,0x00000000,0x0000ffff,0x77096c7d,
                0x20014801,0x78250102,0xbe764eff,0xfe103d29,
                0x20014801,0x78270102,0xbe764eff,0xfe107c6f,
                0x24011800,0x78000104,0xbe764eff,0xfe1c05d6,
                0xfd87d87e,0xeb43eb1d,0x37eb6e86,0xbf963a5d,
                0xfd87d87e,0xeb4359c8,0xaaf757cc,0xcd97a9dd,
                0xfd87d87e,0xeb43b873,0x46597a50,0x0b332a92,
                0xfd87d87e,0xeb43cd71,0x46849e51,0xaf91e5b3
        };
        /* cryptodad May 2019 end */

        bip32HeaderP2PKHpub = 0x043587cf; // The 4 byte header that serializes in base58 to "tpub".
        bip32HeaderP2PKHpriv = 0x04358394; // The 4 byte header that serializes in base58 to "tprv"
        bip32HeaderP2WPKHpub = 0x045f1cf6; // The 4 byte header that serializes in base58 to "vpub".
        bip32HeaderP2WPKHpriv = 0x045f18bc; // The 4 byte header that serializes in base58 to "vprv"

        majorityEnforceBlockUpgrade = TESTNET_MAJORITY_ENFORCE_BLOCK_UPGRADE;
        majorityRejectBlockOutdated = TESTNET_MAJORITY_REJECT_BLOCK_OUTDATED;
        majorityWindow = TESTNET_MAJORITY_WINDOW;
    }

    private static TestNet4Params instance;

    public static synchronized TestNet4Params get() {
        if (instance == null) {
            instance = new TestNet4Params();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return PAYMENT_PROTOCOL_ID_TESTNET;
    }

    // February 16th 2012
    private static final Date testnetDiffDate = new Date(1550577517L);

    /* cryptodad May 2019 - now done in AbstractBlockChain */
/*
    @Override
    public void checkDifficultyTransitions(final StoredBlock storedPrev, final Block nextBlock,
        final BlockStore blockStore) throws VerificationException, BlockStoreException {
        if (!isDifficultyTransitionPoint(storedPrev.getHeight()) && nextBlock.getTime().after(testnetDiffDate)) {
            Block prev = storedPrev.getHeader();

            // After 15th February 2012 the rules on the testnet change to avoid people running up the difficulty
            // and then leaving, making it too hard to mine a block. On non-difficulty transition points, easy
            // blocks are allowed if there has been a span of 20 minutes without one.
            final long timeDelta = nextBlock.getTimeSeconds() - prev.getTimeSeconds();
            // There is an integer underflow bug in bitcoin-qt that means mindiff blocks are accepted when time
            // goes backwards.
            if (timeDelta >= 0 && timeDelta <= NetworkParameters.TARGET_SPACING * 2) {
                // Walk backwards until we find a block that doesn't have the easiest proof of work, then check
                // that difficulty is equal to that one.
                StoredBlock cursor = storedPrev;
                while (!cursor.getHeader().equals(getGenesisBlock()) &&
                           cursor.getHeight() % getInterval() != 0 &&
                           cursor.getHeader().getDifficultyTargetAsInteger().equals(getMaxTarget()))
                        cursor = cursor.getPrev(blockStore);
                BigInteger cursorTarget = cursor.getHeader().getDifficultyTargetAsInteger();
                BigInteger newTarget = nextBlock.getDifficultyTargetAsInteger();
                if (!cursorTarget.equals(newTarget))
                        throw new VerificationException("Testnet block transition that is not allowed: " +
                        Long.toHexString(cursor.getHeader().getDifficultyTarget()) + " vs " +
                        Long.toHexString(nextBlock.getDifficultyTarget()));
            }
        } else {
           super.checkDifficultyTransitions(storedPrev, nextBlock, blockStore);
        }
    }
*/
    /* cryptodad May 2019 end */
}
