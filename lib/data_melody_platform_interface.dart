import 'package:data_melody/enums/data_melody_player_type.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'data_melody_method_channel.dart';

abstract class DataMelodyPlatform extends PlatformInterface {
  /// Constructs a DataMelodyPlatform.
  DataMelodyPlatform() : super(token: _token);

  static final Object _token = Object();

  static DataMelodyPlatform _instance = MethodChannelDataMelody();

  /// The default instance of [DataMelodyPlatform] to use.
  ///
  /// Defaults to [MethodChannelDataMelody].
  static DataMelodyPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [DataMelodyPlatform] when
  /// they register themselves.
  static set instance(DataMelodyPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<void> initialize();

  Future<void> startSendingData({
    required final String data,
    final DataMelodyPlayer player = DataMelodyPlayer.ultrasonicNormal,
    final int volume = 50,
  });

  Future<void> stopSendingData();

  Future<PermissionStatus> requestReceivingPermission();

  Future<void> startReceivingData();

  Future<void> stopReceivingData();

  Stream<Map<String, dynamic>> get receivedData;

  Stream<bool> get isSendingData;

  Stream<bool> get isReceivingData;
}
