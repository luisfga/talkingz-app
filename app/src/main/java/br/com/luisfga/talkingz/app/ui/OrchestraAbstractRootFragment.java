package br.com.luisfga.talkingz.app.ui;

import androidx.fragment.app.Fragment;

import br.com.luisfga.talkingz.app.background.TalkinzApp;

public abstract class OrchestraAbstractRootFragment extends Fragment {

    protected TalkinzApp getTalkingzApp(){
        return (TalkinzApp) getActivity().getApplication();
    }
}
