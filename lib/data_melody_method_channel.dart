import 'package:data_melody/enums/data_melody_player_type.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';

import 'data_melody_platform_interface.dart';

/// An implementation of [DataMelodyPlatform] that uses method channels.
class MethodChannelDataMelody extends DataMelodyPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('data_melody');

  /// The event channel used to interact with the native platform.
  @visibleForTesting
  final eventChannel = const EventChannel('data_melody_event_channel');

  @override
  Future<void> initialize() {
    return methodChannel.invokeMethod('initialize');
  }

  @override
  Future<void> startReceivingData() async {
    final permissionStatus = await requestReceivingPermission();
    if (!permissionStatus.isGranted) {
      throw FlutterError('Permission.microphone is not granted');
    }

    return methodChannel.invokeMethod('startReceivingData');
  }

  @override
  Future<void> startSendingData({
    required final String data,
    final DataMelodyPlayer player = DataMelodyPlayer.ultrasonicNormal,
    final int volume = 50,
  }) {
    final dataToSend = <String, dynamic>{
      'data': data,
      'player': player.name,
      'volume': volume,
    };

    return methodChannel.invokeMethod('startSendingData', dataToSend);
  }

  @override
  Future<void> stopReceivingData() {
    return methodChannel.invokeMethod('stopReceivingData');
  }

  @override
  Future<void> stopSendingData() {
    return methodChannel.invokeMethod('stopSendingData');
  }

  @override
  Stream<Map<String, dynamic>> get receivedData =>
      eventChannel.receiveBroadcastStream().map(
            (event) => Map<String, dynamic>.from(event),
          );

  @override
  Future<PermissionStatus> requestReceivingPermission() async {
    return await Permission.microphone.request();
  }
}
