package org.synopia.audio.core;

import java.util.List;

/**
 * Created by synopia on 19.09.2015.
 */
public interface HasInput extends AudioNode {
    void addInput(HasOutput input);

    List<HasOutput> inputs();
}
