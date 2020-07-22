/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InflightReloader {
  private static final Logger _logger = LoggerFactory.getLogger(InflightReloader.class);

  public static final String RELOAD_POLL_PERIOD_PROPERTY = "com.cleversafe.reloader.timeout";
  private static final int DEFAULT_POLL_PERIOD = 3000;

  private static InflightReloader instance;

  private int pollPeriod = DEFAULT_POLL_PERIOD;
  private final Set<ReloadableFile> watchList =
      Collections.synchronizedSet(new HashSet<ReloadableFile>());
  private final AtomicBoolean running = new AtomicBoolean(false);
  private Thread thread;

  private InflightReloader() {

  }

  public interface ReloadEvent {
    void signal(File path);
  }

  private static class ReloadableFile {
    private final File file;
    private long lastModified;
    private final ReloadEvent event;

    public ReloadableFile(final File file, final ReloadEvent event) {
      this.file = file;
      this.lastModified = this.file.lastModified();
      this.event = event;
    }

    public boolean checkModAndSignal() {
      if (_logger.isTraceEnabled()) {
        _logger.trace("Checking for file modifcations: {}", this.file);

        if (!this.file.exists() && this.lastModified > 0L) {
          _logger.trace("File was removed: {}", this.file);
        }
      }

      boolean isModified = false;
      final long mostRecentModification = this.file.lastModified();

      if ((!this.file.exists() && this.lastModified > 0L)
          || mostRecentModification > this.lastModified) {
        this.lastModified = mostRecentModification;
        isModified = true;

        this.event.signal(this.file);
        _logger.info("Detected change in {}. Reconfiguring...", this.file);
      } else {
        _logger.trace("File was not modified: {}", this.file);
      }

      return isModified;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((this.file == null) ? 0 : this.file.hashCode());
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
      final ReloadableFile other = (ReloadableFile) obj;
      if (this.file == null) {
        if (other.file != null) {
          return false;
        }
      } else if (!this.file.equals(other.file)) {
        return false;
      }
      return true;
    }

  }

  public void start() {
    if (this.running.compareAndSet(false, true)) {
      this.pollPeriod = Integer.getInteger(RELOAD_POLL_PERIOD_PROPERTY, DEFAULT_POLL_PERIOD);

      this.thread = new Thread("Inflight Reloader") {
        @Override
        public void run() {
          while (InflightReloader.this.running.get()) {
            try {
              Thread.sleep(InflightReloader.this.pollPeriod);
            } catch (final InterruptedException e) {
              _logger.trace(e.getMessage(), e);
              Thread.currentThread().interrupt();
              throw new IllegalStateException(e);
            }

            final Set<ReloadableFile> watchListSnapshot =
                new HashSet<>(InflightReloader.this.watchList);
            for (final ReloadableFile reloadable : watchListSnapshot) {
              reloadable.checkModAndSignal();
            }

          }
        }
      };
      this.thread.setDaemon(true);
      this.thread.start();
    }
  }

  public void stop() {
    this.running.set(false);
    this.thread.interrupt();
  }

  public void registerPath(final File path, final ReloadEvent event) {
    final ReloadableFile reloadableFile = new ReloadableFile(path, event);
    if (!this.watchList.add(reloadableFile)) {
      synchronized (this.watchList) {
        this.watchList.remove(reloadableFile);
        this.watchList.add(reloadableFile);
      }
    }
  }

  public void registerPath(final URI url, final ReloadEvent event) {
    try {
      final File path = new File(url);
      registerPath(path, event);
    } catch (final IllegalArgumentException e) {
      _logger.debug("Local configuration is not a file; will not monitor for changes", e);
    }
  }

  public static synchronized InflightReloader getInstance() {
    if (instance == null) {
      instance = new InflightReloader();
      instance.start();
    }
    return instance;
  }

}
