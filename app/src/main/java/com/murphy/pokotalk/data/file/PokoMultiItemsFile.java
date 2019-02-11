package com.murphy.pokotalk.data.file;

import java.util.ArrayList;

/** Extends PokoFile with multiple items feature */
public abstract class PokoMultiItemsFile<T> extends PokoFile<T> {
    protected ArrayList<T> dataList;

}
