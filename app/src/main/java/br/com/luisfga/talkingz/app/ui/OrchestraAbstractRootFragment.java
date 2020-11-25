package br.com.luisfga.talkingz.app.ui;

import androidx.fragment.app.Fragment;

import br.com.luisfga.talkingz.app.TalkingzApp;

public abstract class OrchestraAbstractRootFragment extends Fragment {

    protected TalkingzApp getTalkingzApp(){
        return (TalkingzApp) getActivity().getApplication();
    }
}
