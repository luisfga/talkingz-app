package br.com.luisfga.talkingz.app.ui;

import androidx.fragment.app.Fragment;

import br.com.luisfga.talkingz.app.background.OrchestraApp;

public abstract class OrchestraAbstractRootFragment extends Fragment {

    protected OrchestraApp getOrchestraApp(){
        return (OrchestraApp) getActivity().getApplication();
    }
}
