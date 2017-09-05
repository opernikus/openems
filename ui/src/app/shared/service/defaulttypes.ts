import * as moment from 'moment';

export module DefaultTypes {

  export interface ChannelAddresses {
    [thing: string]: string[];
  }

  export interface Config {
    things: {
      [id: string]: {
        id: string,
        class: string | string[],
        [channel: string]: any
      }
    },
    meta: {
      [clazz: string]: {
        implements: [string],
        channels: {
          [channel: string]: {
            name: string,
            title: string,
            type: string | string[],
            optional: boolean,
            array: boolean,
            accessLevel: string
          }
        }
      }
    }
  }

  export interface Data {
    [thing: string]: {
      [channel: string]: any
    }
  }

  export interface HistoricData {
    data: [{
      time: string,
      channels: Data
    }]
  }

  export interface Summary {
    storage: {
      soc: number,
      activePower: number,
      maxActivePower: number
    }, production: {
      powerRatio: number,
      activePower: number, // sum of activePowerAC and activePowerDC
      activePowerAC: number,
      activePowerDC: number,
      maxActivePower: number
    }, grid: {
      powerRatio: number,
      activePower: number,
      maxActivePower: number,
      minActivePower: number
    }, consumption: {
      powerRatio: number,
      activePower: number
    }
  }

  export interface MessageMetadataDevice {
    name: string,
    comment: string,
    producttype: string,
    role: string,
    online: boolean
  }

  export type NotificationType = "success" | "error" | "warning" | "info";

  export interface Notification {
    type: NotificationType;
    message: string;
    code?: number,
    params?: string[]
  }

  export interface Log {
    time: number | string,
    level: string,
    source: string,
    message: string,
    color?: string /* is added later */
  }
}