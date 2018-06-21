/*
 *  Copyright 2018 jd.com
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.yihaodian.architecture.kira.common;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ComponentAdaptor implements IComponent {

  protected final AtomicInteger componentState = new AtomicInteger(
      ComponentStateEnum.INIT.getState());
  protected final ReadWriteLock lockForComponentState = new ReentrantReadWriteLock();
  protected Logger logger = LoggerFactory.getLogger(this.getClass());
  protected String componentId = this.getClass().getSimpleName();
  protected Date lastStartedTime;
  //When requestShutdown=true, all works need to be aborted and do not retry anything. Add this to support quick response.
  protected volatile boolean requestShutdown;
  //When requestDestroy=true, all works need to be aborted and do not retry anything. It means this instance will be destroyed soon and should not be used again. Add this to support quick response.
  protected volatile boolean requestDestroy;

  public ComponentAdaptor() throws Exception {
    // TODO Auto-generated constructor stub
  }

  @Override
  public String getComponentId() {
    return this.componentId;
  }

  protected boolean isInInitState() {
    boolean returnValue = false;
    try {
      lockForComponentState.readLock().lock();
      try {
        returnValue = (ComponentStateEnum.INIT.getState() == this.componentState.get());
      } finally {
        lockForComponentState.readLock().unlock();
      }
    } catch (Exception e) {
      logger.error("Error occurs for isInInitState(). componentId=" + this.getComponentId()
          + " and componentState=" + this.componentState, e);
    }

    return returnValue;
  }

  @Override
  public boolean isStarted() {
    boolean returnValue = false;
    try {
      lockForComponentState.readLock().lock();
      try {
        returnValue = (ComponentStateEnum.STARTED.getState() == this.componentState.get());
      } finally {
        lockForComponentState.readLock().unlock();
      }
    } catch (Exception e) {
      logger.error("Error occurs for isStarted(). componentId=" + this.getComponentId()
          + " and componentState=" + this.componentState, e);
    }

    return returnValue;
  }

  @Override
  public boolean isStarted(boolean accurate) {
    boolean returnValue = false;
    if (accurate) {
      returnValue = this.isStarted();
    } else {
      returnValue = (ComponentStateEnum.STARTED.getState() == this.componentState.get());
    }

    return returnValue;
  }

  @Override
  public Date getLastStartedTime() {
    return lastStartedTime;
  }

  @Override
  public boolean isShuttingdown() {
    boolean returnValue = false;
    try {
      lockForComponentState.readLock().lock();
      try {
        returnValue = (ComponentStateEnum.SHUTTINGDOWN.getState() == this.componentState.get());
      } finally {
        lockForComponentState.readLock().unlock();
      }
    } catch (Exception e) {
      logger.error("Error occurs for isShuttingdown(). componentId=" + this.getComponentId()
          + " and componentState=" + this.componentState, e);
    }

    return returnValue;
  }

  @Override
  public boolean isShutdown() {
    boolean returnValue = false;
    try {
      lockForComponentState.readLock().lock();
      try {
        returnValue = (ComponentStateEnum.SHUTDOWN.getState() == this.componentState.get());
      } finally {
        lockForComponentState.readLock().unlock();
      }
    } catch (Exception e) {
      logger.error("Error occurs for isShutdown(). componentId=" + this.getComponentId()
          + " and componentState=" + this.componentState, e);
    }

    return returnValue;
  }

  protected boolean isDestroying() {
    boolean returnValue = false;
    try {
      lockForComponentState.readLock().lock();
      try {
        returnValue = (ComponentStateEnum.DESTROYING.getState() == this.componentState.get());
      } finally {
        lockForComponentState.readLock().unlock();
      }
    } catch (Exception e) {
      logger.error("Error occurs for isDestroying(). componentId=" + this.getComponentId()
          + " and componentState=" + this.componentState, e);
    }

    return returnValue;
  }

  protected boolean isDestroyed() {
    boolean returnValue = false;
    try {
      lockForComponentState.readLock().lock();
      try {
        returnValue = (ComponentStateEnum.DESTROYED.getState() == this.componentState.get());
      } finally {
        lockForComponentState.readLock().unlock();
      }
    } catch (Exception e) {
      logger.error("Error occurs for isDestroyed(). componentId=" + this.getComponentId()
          + " and componentState=" + this.componentState, e);
    }

    return returnValue;
  }

  @Override
  public boolean isRequestShutdown() {
    return this.requestShutdown;
  }

  @Override
  public boolean isRequestDestroy() {
    return this.requestDestroy;
  }

  @Override
  public void start() {
    //always set requestShutdown to false for it can be restarted.
    this.requestShutdown = false;
    boolean needRetry = true;
    do {
      try {
        this.lockForComponentState.writeLock().lock();
        try {
          if (!this.requestShutdown && !this.requestDestroy) {
            if (this.componentState.compareAndSet(ComponentStateEnum.INIT.getState(),
                ComponentStateEnum.STARTING.getState())
                || this.componentState.compareAndSet(ComponentStateEnum.SHUTDOWN.getState(),
                ComponentStateEnum.STARTING.getState())
                || ComponentStateEnum.STARTING.getState() == this.componentState.get()) {
              boolean doStartSuccess = this.doStart();
              if (!doStartSuccess) {
                //will retry ,so reset the state
                logger.error(this.getClass().getSimpleName() + ": doStart() failed. componentId={}",
                    this.componentId);
              } else {
                this.componentState.set(ComponentStateEnum.STARTED.getState());
                this.lastStartedTime = new Date();
                needRetry = false;
              }
            } else {
              if (this.requestShutdown || this.requestDestroy) {
                needRetry = false;
                logger.warn(this.getClass().getSimpleName()
                        + " may request shutdown or destroy. So do not retry start. componentId={} and componentState={} and requestShutdown={} and requestDestroy={}",
                    this.componentId, this.componentState, this.requestShutdown,
                    this.requestDestroy);
              } else {
                if (this.isStarted()) {
                  needRetry = false;
                  logger.warn(this.getClass().getSimpleName()
                      + " no need to start for it is already in the started state. componentId="
                      + this.componentId + " and componentState=" + this.componentState);
                } else {
                  needRetry = false;
                  String errorMessage = this.getClass().getSimpleName()
                      + " failed to start. It may not be in correct state. May have bugs. componentId="
                      + this.componentId + " and componentState=" + this.componentState;
                  logger.error(errorMessage);
                }
              }
            }
          } else {
            needRetry = false;
            logger.warn(this.getClass().getSimpleName()
                    + " may request shutdown or destroy. So do not continue to start. componentId={} and componentState={} and requestShutdown={} and requestDestroy={}",
                this.componentId, this.componentState, this.requestShutdown, this.requestDestroy);
          }
        } finally {
          this.lockForComponentState.writeLock().unlock();
        }
      } catch (Exception e) {
        logger.error("Error occurs during call start() in " + this.getClass().getSimpleName()
            + ". componentId=" + this.componentId, e);
      } catch (Error e) {
        logger.error("Error occurs during call start() in " + this.getClass().getSimpleName()
            + " . So do not retry. componentId=" + this.componentId, e);
        //If error occurs do not retry for it may has serious error occurs.
        needRetry = false;
      } finally {
        if (needRetry) {
          try {
            logger.info("Will retry start " + this.getClass().getSimpleName() + "...");
            try {
              Thread.sleep(KiraCommonConstants.RETRY_INTERVAL_MILLISECOND);
            } catch (InterruptedException e1) {
              if (this.requestShutdown || this.requestDestroy) {
                needRetry = false;
                logger.warn(this.getClass().getSimpleName()
                        + " may request destroy. So do not retry start. componentId={} and componentState={}",
                    this.componentId, this.componentState);
              }
              Thread.currentThread().interrupt();
            }
          } catch (Exception e) {
            logger.error("Error occurs during wait for retry to start() for " + this.getClass()
                .getSimpleName() + ". componentId=" + this.componentId, e);
          }
        }
      }
    } while (needRetry && !this.requestShutdown && !this.requestDestroy);
  }

  protected boolean doStart() {
    return true;
  }

  @Override
  public void shutdown() {
    try {
      this.requestShutdown = true;
      this.lockForComponentState.writeLock().lock();
      try {
        if (this.componentState.compareAndSet(ComponentStateEnum.STARTED.getState(),
            ComponentStateEnum.SHUTTINGDOWN.getState())
            || this.componentState.compareAndSet(ComponentStateEnum.STARTING.getState(),
            ComponentStateEnum.SHUTTINGDOWN.getState())) {
          try {
            this.doShutdown();
          } finally {
            this.componentState.set(ComponentStateEnum.SHUTDOWN.getState());
          }
        } else {
          if (this.requestDestroy) {
            logger.warn(this.getClass().getSimpleName()
                    + " may request destroy. But it is already not in the started state. So do nothing for shutdown. componentState={}",
                this.componentState);
          } else {
            if (this.isInInitState()) {
              logger.warn(
                  this.getClass().getSimpleName() + " is in init state. So no need to shutdown.");
            } else if (this.isShutdown()) {
              logger.warn(this.getClass().getSimpleName()
                  + " is already in shutdown state. So do not shutdown again.");
            } else {
              String errorMessage = "Failed to shutdown " + this.getClass().getSimpleName()
                  + ". It may not in started state. componentState=" + this.componentState;
              logger.error(errorMessage);
            }
          }
        }
      } finally {
        this.lockForComponentState.writeLock().unlock();
      }
    } catch (Throwable t) {
      logger.error("Error occurs when call shutdown() for " + this.getClass().getSimpleName(), t);
    }
  }

  protected void doShutdown() {

  }

  @Override
  public void restart() {
    throw new RuntimeException("restart is not implemented or supported.");
  }

  @Override
  public void destroy() {
    try {
      logger.info("Calling destroy() for " + this.getClass().getSimpleName() + "...");
      this.requestDestroy = true;

      long startTime = System.currentTimeMillis();
      this.lockForComponentState.writeLock().lock();
      try {
        try {
          if (!this.isShutdown()) {
            this.shutdown();
          }

          this.componentState.set(ComponentStateEnum.DESTROYING.getState());
          logger.info("Destroying " + this.getClass().getSimpleName() + "...");
          this.doDestroy();
          logger.info("Successfully destroy " + this.getClass().getSimpleName());
        } finally {
          //always set the state to destroyed
          this.componentState.set(ComponentStateEnum.DESTROYED.getState());
          long costTime = System.currentTimeMillis() - startTime;
          logger.info(
              "Finish destroy " + this.getClass().getSimpleName() + ". It takes {} milliseconds.",
              costTime);
        }
      } finally {
        this.lockForComponentState.writeLock().unlock();
      }
    } catch (Throwable t) {
      logger.error("Error occurs when call destroy() for " + this.getClass().getSimpleName(), t);
    } finally {
      logger.info("Finish call destroy() for " + this.getClass().getSimpleName());
    }
  }

  protected void doDestroy() {

  }

}
