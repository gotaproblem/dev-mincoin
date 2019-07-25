/*
 * Copyright the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.schildbach.wallet.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.mincoinj.core.Address;
import org.mincoinj.core.ECKey;
import org.mincoinj.core.LegacyAddress;
import org.mincoinj.utils.Threading;
import org.mincoinj.wallet.Wallet;
import org.mincoinj.wallet.listeners.KeyChainEventListener;

import de.schildbach.wallet.Constants;
import de.schildbach.wallet.WalletApplication;
import de.schildbach.wallet.data.AbstractWalletLiveData;
import de.schildbach.wallet.data.AddressBookEntry;
import de.schildbach.wallet.data.AppDatabase;
import de.schildbach.wallet.data.ConfigOwnNameLiveData;
import de.schildbach.wallet.data.WalletLiveData;

import android.app.Application;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * @author Andreas Schildbach
 */
public class WalletAddressesViewModel extends AndroidViewModel {
    private final WalletApplication application;
    public final IssuedReceiveAddressesLiveData issuedReceiveAddresses;
    public final ImportedAddressesLiveData importedAddresses;
    public final LiveData<List<AddressBookEntry>> addressBook;
    public final WalletLiveData wallet;
    public final ConfigOwnNameLiveData ownName;
    public final MutableLiveData<Event<Bitmap>> showBitmapDialog = new MutableLiveData<>();
    public final MutableLiveData<Event<Address>> showEditAddressBookEntryDialog = new MutableLiveData<>();

    public WalletAddressesViewModel(final Application application) {
        super(application);
        this.application = (WalletApplication) application;
        this.issuedReceiveAddresses = new IssuedReceiveAddressesLiveData(this.application);
        this.importedAddresses = new ImportedAddressesLiveData(this.application);
        this.addressBook = AppDatabase.getDatabase(this.application).addressBookDao().getAll();
        this.wallet = new WalletLiveData(this.application);
        this.ownName = new ConfigOwnNameLiveData(this.application);
    }

    public static class IssuedReceiveAddressesLiveData extends AbstractWalletLiveData<List<Address>>
            implements KeyChainEventListener {
        public IssuedReceiveAddressesLiveData(final WalletApplication application) {
            super(application);
        }

        @Override
        protected void onWalletActive(final Wallet wallet) {
            wallet.addKeyChainEventListener(Threading.SAME_THREAD, this);
            loadAddresses();
        }

        @Override
        protected void onWalletInactive(final Wallet wallet) {
            wallet.removeKeyChainEventListener(this);
        }

        @Override
        public void onKeysAdded(final List<ECKey> keys) {
            loadAddresses();
        }

        private void loadAddresses() {
            final Wallet wallet = getWallet();
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    postValue(wallet.getIssuedReceiveAddresses());
                }
            });
        }
    }

    public static class ImportedAddressesLiveData extends AbstractWalletLiveData<List<Address>>
            implements KeyChainEventListener {
        public ImportedAddressesLiveData(final WalletApplication application) {
            super(application);
        }

        @Override
        protected void onWalletActive(final Wallet wallet) {
            wallet.addKeyChainEventListener(Threading.SAME_THREAD, this);
            loadAddresses();
        }

        @Override
        protected void onWalletInactive(final Wallet wallet) {
            wallet.removeKeyChainEventListener(this);
        }

        @Override
        public void onKeysAdded(final List<ECKey> keys) {
            loadAddresses();
        }

        private void loadAddresses() {
            final Wallet wallet = getWallet();
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    final List<ECKey> importedKeys = wallet.getImportedKeys();
                    Collections.sort(importedKeys, new Comparator<ECKey>() {
                        @Override
                        public int compare(final ECKey lhs, final ECKey rhs) {
                            final boolean lhsRotating = wallet.isKeyRotating(lhs);
                            final boolean rhsRotating = wallet.isKeyRotating(rhs);

                            if (lhsRotating != rhsRotating)
                                return lhsRotating ? 1 : -1;
                            if (lhs.getCreationTimeSeconds() != rhs.getCreationTimeSeconds())
                                return lhs.getCreationTimeSeconds() > rhs.getCreationTimeSeconds() ? 1 : -1;
                            return 0;
                        }
                    });
                    final List<Address> importedAddresses = new ArrayList<>();
                    for (final ECKey key : importedKeys)
                        importedAddresses.add(LegacyAddress.fromKey(Constants.NETWORK_PARAMETERS, key));
                    postValue(importedAddresses);
                }
            });
        }
    }
}
