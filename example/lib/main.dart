import 'package:data_melody/data_melody.dart';
import 'package:data_melody/enums/data_melody_player_type.dart';
import 'package:flutter/material.dart';
import 'package:syncfusion_flutter_sliders/sliders.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await DataMelody.initialize();

  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _dataMelody = DataMelody();

  late Stream<Map<String, dynamic>> _receivedDataStream;
  late Stream<bool> _isSendingDataStream;

  final _messageController = TextEditingController(text: 'hello world');
  var _selectedPlayerType = DataMelodyPlayer.ultrasonicNormal;
  var _volume = 50;

  @override
  void initState() {
    super.initState();
    _receivedDataStream = _dataMelody.receivedData;
    _isSendingDataStream = _dataMelody.isSendingData;

    _startListening();
  }

  void _startListening() async {
    await _dataMelody.startReceivingData();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: SingleChildScrollView(
            child: Padding(
              padding: const EdgeInsets.symmetric(vertical: 20.0),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  _inputBuilder().pX(20),
                  const SizedBox(
                    height: 50.0,
                  ),
                  _volumeSlider(),
                  const SizedBox(
                    height: 50.0,
                  ),
                  _playerSelector().pX(20),
                  const SizedBox(
                    height: 50.0,
                  ),
                  _buttonBuilder().pX(20),
                  const SizedBox(
                    height: 50.0,
                  ),
                  _receivedDataBuilder().pX(20),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _inputBuilder() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text('Message:'),
        const SizedBox(
          height: 15.0,
        ),
        TextFormField(
          controller: _messageController,
          decoration: const InputDecoration(
            border: OutlineInputBorder(),
            hintText: 'Message',
          ),
        ),
      ],
    );
  }

  Widget _volumeSlider() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text('Volume:').pX(20),
        SfSlider(
          min: 0,
          max: 100,
          value: _volume,
          interval: 10,
          showTicks: true,
          showLabels: true,
          enableTooltip: true,
          minorTicksPerInterval: 1,
          onChanged: (value) {
            setState(() {
              if (value is double) {
                _volume = value.toInt();
              } else if (value is int) {
                _volume = value;
              }
            });
          },
        ),
      ],
    );
  }

  Widget _playerSelector() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text('Player Type:'),
        const SizedBox(
          height: 10.0,
        ),
        DropdownButton<DataMelodyPlayer>(
          value: _selectedPlayerType,
          iconEnabledColor: Colors.blue,
          isExpanded: true,
          items: DataMelodyPlayer.values.map((e) {
            return DropdownMenuItem<DataMelodyPlayer>(
              value: e,
              child: Text(
                e.name,
                style: TextStyle(
                  color: _selectedPlayerType == e ? Colors.blue : null,
                ),
              ),
            );
          }).toList(),
          onChanged: (val) {
            if (val != null) {
              setState(() {
                _selectedPlayerType = val;
              });
            }
          },
        ),
      ],
    );
  }

  Widget _buttonBuilder() {
    return Row(
      children: [
        Expanded(
          child: StreamBuilder<bool>(
            stream: _isSendingDataStream,
            builder: (context, snapshot) {
              if (snapshot.hasData) {
                final isSendingData = snapshot.data ?? false;

                return ElevatedButton(
                  style: ElevatedButton.styleFrom(
                    backgroundColor: isSendingData ? Colors.red : null,
                  ),
                  onPressed: () async {
                    FocusManager.instance.primaryFocus?.unfocus();

                    if (isSendingData) {
                      await _dataMelody.stopSendingData();
                    } else {
                      await _dataMelody.startSendingData(
                        data: _messageController.text,
                        player: _selectedPlayerType,
                        volume: _volume,
                      );
                    }
                  },
                  child: Text(
                    isSendingData
                        ? 'Stop Playing Sound'
                        : 'Start Playing Sound',
                  ),
                );
              }

              return const SizedBox();
            },
          ),
        ),
      ],
    );
  }

  Widget _receivedDataBuilder() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text('Received Data:'),
        const SizedBox(
          height: 20.0,
        ),
        Center(
          child: StreamBuilder<Map<String, dynamic>>(
            stream: _receivedDataStream,
            builder: (context, snapshot) {
              if (snapshot.hasData) {
                final snapData = snapshot.data!;
                final data = snapData['data'];

                if (data != null) {
                  return Text(
                    '$data',
                    style: const TextStyle(
                      color: Colors.green,
                    ),
                  );
                }
              }

              return const Text(
                'Nothing received yet',
                style: TextStyle(
                  color: Colors.grey,
                  fontStyle: FontStyle.italic,
                ),
              );
            },
          ),
        ),
      ],
    );
  }
}

extension on Widget {
  Widget pX(final double x) {
    return Padding(
      padding: EdgeInsets.symmetric(horizontal: x),
      child: this,
    );
  }
}
