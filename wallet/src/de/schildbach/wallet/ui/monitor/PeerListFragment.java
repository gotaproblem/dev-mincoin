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

package de.schildbach.wallet.ui.monitor;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

import org.mincoinj.core.Peer;

import de.schildbach.wallet.R;
import de.schildbach.wallet.ui.DividerItemDecoration;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewAnimator;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author Andreas Schildbach
 */
public final class PeerListFragment extends Fragment {
    private Activity activity;

    private ViewAnimator viewGroup;
    private RecyclerView recyclerView;
    private PeerListAdapter adapter;

    private PeerListViewModel viewModel;

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        this.activity = (Activity) context;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(PeerListViewModel.class);
        viewModel.getPeers().observe(this, new Observer<List<Peer>>() {
            @Override
            public void onChanged(final List<Peer> peers) {
                viewGroup.setDisplayedChild((peers == null || peers.isEmpty()) ? 1 : 2);
                maybeSubmitList();
                if (peers != null)
                    for (final Peer peer : peers)
                        viewModel.getHostnames().reverseLookup(peer.getAddress().getAddr());
            }
        });
        viewModel.getHostnames().observe(this, new Observer<Map<InetAddress, String>>() {
            @Override
            public void onChanged(final Map<InetAddress, String> hostnames) {
                maybeSubmitList();
            }
        });

        adapter = new PeerListAdapter(activity);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.peer_list_fragment, container, false);

        viewGroup = (ViewAnimator) view.findViewById(R.id.peer_list_group);

        recyclerView = (RecyclerView) view.findViewById(R.id.peer_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        return view;
    }

    private void maybeSubmitList() {
        final List<Peer> peers = viewModel.getPeers().getValue();
        if (peers != null)
            adapter.submitList(PeerListAdapter.buildListItems(activity, peers, viewModel.getHostnames().getValue()));
    }
}
