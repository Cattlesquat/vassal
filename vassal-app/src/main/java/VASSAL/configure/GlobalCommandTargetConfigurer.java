package VASSAL.configure;

import VASSAL.build.module.Map;
import VASSAL.counters.GlobalCommandTarget;
import java.awt.Component;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;

public class GlobalCommandTargetConfigurer extends Configurer {

  private JPanel controls;
  private BooleanConfigurer useLocationConfig;
  private BooleanConfigurer usePropertyConfig;
  private TranslatingStringEnumConfigurer targetTypeConfig;
  private JLabel targetTypeLabel;

  private FormattedStringConfigurer targetMapConfig;
  private JLabel targetMapLabel;
  private FormattedStringConfigurer targetBoardConfig;
  private JLabel targetBoardLabel;
  private FormattedStringConfigurer targetZoneConfig;
  private JLabel targetZoneLabel;
  private FormattedStringConfigurer targetLocationConfig;
  private JLabel targetLocationLabel;
  private IntConfigurer targetXConfig;
  private JLabel targetXLabel;
  private IntConfigurer targetYConfig;
  private JLabel targetYLabel;

  private FormattedStringConfigurer targetPropertyConfig;
  private JLabel targetPropertyLabel;
  private FormattedStringConfigurer targetValueConfig;
  private JLabel targetValueLabel;

  public GlobalCommandTargetConfigurer(String key, String name, GlobalCommandTarget target) {
    super(key, name, target);
  }

  public GlobalCommandTargetConfigurer(String key, String name) {
    this(key, name, null);
  }

  public GlobalCommandTargetConfigurer(GlobalCommandTarget target) {
    this(null, null, target);
  }

  public GlobalCommandTarget getTarget() {
    return (GlobalCommandTarget) getValue();
  }

  @Override
  public String getValueString() {
    return getTarget().encode();
  }

  @Override
  public void setValue(Object o) {
    super.setValue(o);
  }

  @Override
  public void setValue(String s) {
    setValue(new GlobalCommandTarget(s));
  }

  @Override
  public Component getControls() {
    if (controls == null) {
      controls = new JPanel(new MigLayout("hidemode 3, ins 1, gapy 1", "[]rel[]rel[fill,grow]")); // NON-NLS
      final GlobalCommandTarget target = getTarget();

      useLocationConfig = new BooleanConfigurer (target.isUseLocation());
      useLocationConfig.addPropertyChangeListener(evt -> targetChanged());
      controls.add(useLocationConfig.getControls());
      controls.add(new JLabel("by Location?"), "wrap");

      final List<String> options = new ArrayList<>();
      final List<String> i18nKeys = new ArrayList<>();
      if (getTarget().isCounterGkc()) {
        options.add(GlobalCommandTarget.Target.CURSTACK.toString());
        options.add(GlobalCommandTarget.Target.CURMAP.toString());
        options.add(GlobalCommandTarget.Target.CURZONE.toString());
        options.add(GlobalCommandTarget.Target.CURLOC.toString());
        i18nKeys.add(GlobalCommandTarget.Target.CURSTACK.toTranslatedString());
        i18nKeys.add(GlobalCommandTarget.Target.CURMAP.toTranslatedString());
        i18nKeys.add(GlobalCommandTarget.Target.CURZONE.toTranslatedString());
        i18nKeys.add(GlobalCommandTarget.Target.CURLOC.toTranslatedString());
      }
      options.add(GlobalCommandTarget.Target.MAP.toString());
      options.add(GlobalCommandTarget.Target.ZONE.toString());
      options.add(GlobalCommandTarget.Target.LOCATION.toString());
      options.add(GlobalCommandTarget.Target.XY.toString());
      i18nKeys.add(GlobalCommandTarget.Target.MAP.toTranslatedString());
      i18nKeys.add(GlobalCommandTarget.Target.ZONE.toTranslatedString());
      i18nKeys.add(GlobalCommandTarget.Target.LOCATION.toTranslatedString());
      i18nKeys.add(GlobalCommandTarget.Target.XY.toTranslatedString());

      targetTypeConfig = new TranslatingStringEnumConfigurer(options, i18nKeys, target.getTargetType().toString());
      targetTypeConfig.addPropertyChangeListener(evt -> targetChanged());
      targetTypeLabel = new JLabel("Select by");
      controls.add(targetTypeLabel, "span 2");
      controls.add(targetTypeConfig.getControls(), "growx, wrap");

      final List<String> mapNames = new ArrayList<>();
      for (Map map : Map.getMapList()) {
        mapNames.add(map.getMapName());
      }
      targetMapConfig = new FormattedStringConfigurer(null, null);
      targetMapConfig.setNon$Options(mapNames.toArray(new String[0]));
      targetMapConfig.setValue(target.getTargetMap());
      targetMapConfig.addPropertyChangeListener(evt -> getTarget().setTargetMap(targetMapConfig.getValueString()));
      targetMapLabel = new JLabel("Map Name");
      controls.add(targetMapLabel, "span 2");
      controls.add(targetMapConfig.getControls(), "growx, wrap");

      targetBoardConfig = new FormattedStringConfigurer(null, null);
      targetBoardConfig.setValue(target.getTargetBoard());
      targetBoardConfig.addPropertyChangeListener(evt -> getTarget().setTargetBoard(targetBoardConfig.getValueString()));
      targetBoardLabel = new JLabel("Board Name");
      controls.add(targetBoardLabel, "span 2");
      controls.add(targetBoardConfig.getControls(), "growx, wrap");
      
      targetZoneConfig = new FormattedStringConfigurer(null, null);
      targetZoneConfig.setValue(target.getTargetZone());
      targetZoneConfig.addPropertyChangeListener(evt -> getTarget().setTargetZone(targetZoneConfig.getValueString()));
      targetZoneLabel = new JLabel("Zone Name");
      controls.add(targetZoneLabel, "span 2");
      controls.add(targetZoneConfig.getControls(), "growx, wrap");

      targetLocationConfig = new FormattedStringConfigurer(null, null);
      targetLocationConfig.setValue(target.getTargetLocation());
      targetLocationConfig.addPropertyChangeListener(evt -> getTarget().setTargetLocation(targetLocationConfig.getValueString()));
      targetLocationLabel = new JLabel("Location Name");
      controls.add(targetLocationLabel, "span 2");
      controls.add(targetLocationConfig.getControls(), "growx, wrap");

      targetXConfig = new IntConfigurer(target.getTargetX());
      targetXConfig.addPropertyChangeListener(evt -> getTarget().setTargetX(targetXConfig.getIntValue(0)));
      targetXLabel = new JLabel("X Position");
      controls.add(targetXLabel, "span 2");
      controls.add(targetXConfig.getControls(), "growx, wrap");

      targetYConfig = new IntConfigurer(target.getTargetY());
      targetYConfig.addPropertyChangeListener(evt -> getTarget().setTargetY(targetYConfig.getIntValue(0)));
      targetYLabel = new JLabel("Y Position");
      controls.add(targetYLabel, "span 2");
      controls.add(targetYConfig.getControls(), "growY, wrap");
      
      usePropertyConfig = new BooleanConfigurer (target.isUseProperty());
      usePropertyConfig.addPropertyChangeListener(evt -> usePropertyChanged());
      controls.add(usePropertyConfig.getControls());
      controls.add(new JLabel("by Property?"), "wrap");

      targetPropertyConfig = new FormattedExpressionConfigurer (null, null, target.getTargetProperty());
      targetPropertyConfig.addPropertyChangeListener(evt -> getTarget().setTargetProperty(targetPropertyConfig.getValueString()));
      targetPropertyLabel = new JLabel("Property Name");
      controls.add(targetPropertyLabel, "span 2");
      controls.add(targetPropertyConfig.getControls(), "wrap");

      targetValueConfig = new FormattedExpressionConfigurer (null, null, target.getTargetValue());
      targetValueConfig.addPropertyChangeListener(evt -> getTarget().setTargetValue(targetValueConfig.getValueString()));
      targetValueLabel = new JLabel("Property Value");
      controls.add(targetValueLabel, "span 2");
      controls.add(targetValueConfig.getControls(), "wrap");

      targetChanged();
      usePropertyChanged();

      controls.setBorder(BorderFactory.createEtchedBorder());

      if (getName() != null && ! getName().isEmpty()) {
        final JPanel controls2 = controls;
        controls = new JPanel(new MigLayout("ins 0", "[]rel[fill,grow]"));
        controls.add(new JLabel(getName()));
        controls.add(controls2, "growx");
      }
    }

    return controls;
  }

  private void targetChanged() {
    final GlobalCommandTarget target = getTarget();
    
    final boolean useLocation = useLocationConfig.getValueBoolean();
    target.setUseLocation(useLocation);
    
    target.setTargetType(targetTypeConfig.getValueString());
    final GlobalCommandTarget.Target targetType = target.getTargetType();

    targetTypeConfig.getControls().setVisible(useLocation);
    targetTypeLabel.setVisible(useLocation);

    final boolean mapVisible = useLocation && (
      targetType.equals(GlobalCommandTarget.Target.MAP) ||
      targetType.equals(GlobalCommandTarget.Target.ZONE) || 
      targetType.equals(GlobalCommandTarget.Target.LOCATION) ||
      targetType.equals(GlobalCommandTarget.Target.XY));

    targetMapConfig.getControls().setVisible(useLocation && mapVisible);
    targetMapLabel.setVisible(useLocation && mapVisible);
    targetBoardConfig.getControls().setVisible(useLocation && targetType.equals(GlobalCommandTarget.Target.XY));
    targetBoardLabel.setVisible(useLocation && targetType.equals(GlobalCommandTarget.Target.XY));
    targetZoneConfig.getControls().setVisible(useLocation && targetType.equals(GlobalCommandTarget.Target.ZONE));
    targetZoneLabel.setVisible(useLocation && targetType.equals(GlobalCommandTarget.Target.ZONE));
    targetLocationConfig.getControls().setVisible(useLocation && targetType.equals(GlobalCommandTarget.Target.LOCATION));
    targetLocationLabel.setVisible(useLocation && targetType.equals(GlobalCommandTarget.Target.LOCATION));
    targetXConfig.getControls().setVisible(useLocation && targetType.equals(GlobalCommandTarget.Target.XY));
    targetXLabel.setVisible(useLocation && targetType.equals(GlobalCommandTarget.Target.XY));
    targetYConfig.getControls().setVisible(useLocation && targetType.equals(GlobalCommandTarget.Target.XY));
    targetYLabel.setVisible(useLocation && targetType.equals(GlobalCommandTarget.Target.XY));
    repack();
  }

  private void usePropertyChanged() {
    final GlobalCommandTarget target = getTarget();
    target.setUseProperty(usePropertyConfig.getValueBoolean());
    targetPropertyConfig.getControls().setVisible(target.isUseProperty());
    targetPropertyLabel.setVisible(target.isUseProperty());
    targetValueConfig.getControls().setVisible(target.isUseProperty());
    targetValueLabel.setVisible(target.isUseProperty());
    repack();
  }

  public void repack() {
    Window w = SwingUtilities.getWindowAncestor(controls);
    if (w != null) {
      // Don't let pack make dialog smaller.
      w.setMinimumSize(w.getSize());
      w.pack();
      w.setMinimumSize(null);
    }
  }
}