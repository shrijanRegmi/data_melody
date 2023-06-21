import 'package:data_melody/enums/data_melody_player_type.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';

import 'data_melody_platform_interface.dart';

/// An implementation of [DataMelodyPlatform] that uses method channels.
class MethodChannelDataMelody extends DataMelodyPlatform {
  @visibleForTesting
  final methodChannel = const MethodChannel('data_melody');

  @visibleForTesting
  final receivedDataEventChannel = const EventChannel(
    'event_channels/received_data_event_channel',
  );

  @visibleForTesting
  final isSendingDataEventChannel = const EventChannel(
    'event_channels/is_sending_data_event_channel',
  );

  @visibleForTesting
  final isReceivingDataEventChannel = const EventChannel(
    'event_channels/is_receiving_data_event_channel',
  );

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
  Future<PermissionStatus> requestReceivingPermission() async {
    return await Permission.microphone.request();
  }

  @override
  Stream<Map<String, dynamic>> get receivedData =>
      receivedDataEventChannel.receiveBroadcastStream().map(
            (event) => Map<String, dynamic>.from(event),
          );

  @override
  Stream<bool> get isSendingData =>
      isSendingDataEventChannel.receiveBroadcastStream().map(
            (event) => event as bool,
          );

  @override
  Stream<bool> get isReceivingData =>
      isReceivingDataEventChannel.receiveBroadcastStream().map(
            (event) => event as bool,
          );
}
