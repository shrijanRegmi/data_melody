import 'package:data_melody/data_melody_platform_interface.dart';
import 'package:data_melody/enums/data_melody_player_type.dart';

class DataMelody {
  static Future<void> initialize() {
    return DataMelodyPlatform.instance.initialize();
  }

  Future<void> startSendingData({
    required final String data,
    final DataMelodyPlayer player = DataMelodyPlayer.ultrasonicNormal,
    final int volume = 50,
  }) {
    return DataMelodyPlatform.instance.startSendingData(
      data: data,
      player: player,
      volume: volume,
    );
  }

  Future<void> stopSendingData() {
    return DataMelodyPlatform.instance.stopSendingData();
  }

  Future<void> startReceivingData() {
    return DataMelodyPlatform.instance.startReceivingData();
  }

  Future<void> stopReceivingData() {
    return DataMelodyPlatform.instance.stopReceivingData();
  }
}
