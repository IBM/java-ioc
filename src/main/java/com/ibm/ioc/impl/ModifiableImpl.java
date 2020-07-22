/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.impl;

import java.util.ArrayList;

final class ModifiableImpl<T> implements Modifiable<T> {
  private volatile T value;
  private final ArrayList<ModificationListener<T>> listeners = new ArrayList<>();

  public ModifiableImpl() {
    this(null);
  }

  public ModifiableImpl(final T initialValue) {
    set(initialValue);
  }

  @Override
  public final T get() {
    return this.value;
  }

  public void set(final T newValue) {
    this.value = newValue;
    synchronized (this) {
      for (final ModificationListener<T> listener : this.listeners) {
        listener.updated(newValue);
      }
    }
  }

  @Override
  public synchronized void addModificationListener(final ModificationListener<T> listener) {
    this.listeners.add(listener);
    listener.updated(this.value);
  }

  @Override
  public String toString() {
    return String.valueOf(this.value);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.value == null) ? 0 : this.value.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ModifiableImpl<?> other = (ModifiableImpl<?>) obj;
    if (this.value == null) {
      if (other.value != null) {
        return false;
      }
    } else if (!this.value.equals(other.value)) {
      return false;
    }
    return true;
  }

}
