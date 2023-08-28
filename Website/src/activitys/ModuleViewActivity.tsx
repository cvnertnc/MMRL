import { Page } from "@Components/onsenui/Page";
import { Toolbar } from "@Components/onsenui/Toolbar";
import { useStrings } from "@Hooks/useStrings";
import { Disappear } from "react-disappear";
import VerifiedIcon from "@mui/icons-material/Verified";
import { DeleteRounded, RefreshRounded } from "@mui/icons-material";
import LaunchIcon from "@mui/icons-material/Launch";
import Box from "@mui/material/Box";
import React from "react";
import Fade from "@mui/material/Fade";
import IconButton from "@mui/material/IconButton";
import List from "@mui/material/List";
import ListItem from "@mui/material/ListItem";
import CloseIcon from "@mui/icons-material/Close";
import ArrowForwardIcon from "@mui/icons-material/ArrowForward";
import Typography from "@mui/material/Typography";
import Dialog, { DialogProps } from "@mui/material/Dialog";
import DialogContent from "@mui/material/DialogContent";
import DialogContentText from "@mui/material/DialogContentText";
import DialogTitle from "@mui/material/DialogTitle";
import Button from "@mui/material/Button";
import Avatar from "@mui/material/Avatar";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import ImageList from "@mui/material/ImageList";
import ImageListItem from "@mui/material/ImageListItem";
import ListSubheader from "@mui/material/ListSubheader";
import Stack from "@mui/material/Stack";
import Markdown from "markdown-to-jsx";
import { useActivity } from "@Hooks/useActivity";
import { StyledListItemText } from "@Components/StyledListItemText";
import { parseAndroidVersion } from "@Util/parseAndroidVersion";
import { Magisk } from "@Native/Magisk";
import { useTheme } from "@Hooks/useTheme";
import useShadeColor from "@Hooks/useShadeColor";
import { useSettings } from "@Hooks/useSettings";
import { StyledMarkdown } from "@Components/Markdown/StyledMarkdown";
import { os } from "@Native/Os";
import { Markup } from "@Components/Markdown";
import Alert from "@mui/material/Alert";
import AlertTitle from "@mui/material/AlertTitle";
import { useFilterCategory } from "@Hooks/useCategory";
import { useFormatDate } from "@Hooks/useFormatDate";
import Chip from "@mui/material/Chip";
import CodeRoundedIcon from "@mui/icons-material/CodeRounded";
import Icon from "@Components/Icon";
import CardMedia from "@mui/material/CardMedia";
import DevicesIcon from "@mui/icons-material/Devices";
import ButtonGroup from "@mui/material/ButtonGroup";
import { useSupportIconForUrl } from "@Hooks/useSupportIconForUrl";
import { useRepos } from "@Hooks/useRepos";
import { useStateCallback } from "@Hooks/useStateCallback";
import { Tabbar } from "@Components/onsenui/Tabbar";
import PermDeviceInformationIcon from "@mui/icons-material/PermDeviceInformation";
import DescriptionIcon from "@mui/icons-material/Description";
import BottomNavigation from "@mui/material/BottomNavigation";
import BottomNavigationAction from "@mui/material/BottomNavigationAction";
import Paper from "@mui/material/Paper";
import { BottomToolbar } from "@Components/onsenui/BottomToolbar";
import useMediaQuery from "@mui/material/useMediaQuery";
import { useBaseDialog } from "@Hooks/useBaseDialog";
import { useLog } from "@Hooks/native/useLog";
import { SuFile } from "@Native/SuFile";

type Extra = {
  module: ModuleProps;
  notes_url: string;
  zip_url: string;
  authorData?: any;
  last_update: string;
};

function useMultipleRandom<T>(arr: T[], num: number): T[] {
  const ref = React.useRef<T[]>([...arr].sort(() => 0.5 - Math.random()));

  return ref.current.slice(0, num);
}

const ModuleViewActivity = () => {
  const shade = useShadeColor();
  const { strings } = useStrings();
  const { theme, scheme } = useTheme();
  const { context, extra } = useActivity<Extra>();
  const [open, setOpen] = useStateCallback(false);
  const [scroll, setScroll] = React.useState<DialogProps["scroll"]>("paper");

  const log = useLog("ModuleViewActivity");

  const fullScreen = useMediaQuery(theme.breakpoints.down("md"));

  const { modules } = useRepos();

  const more_modules = useMultipleRandom<Module>(modules, 6);

  const { notes_url, zip_url, authorData, last_update } = extra;
  const {
    id,
    name,
    author,
    mmrlAuthor,
    mmrlLogo,
    mmrlScreenshots,
    version,
    versionCode,
    mmtReborn,
    support,
    minMagisk,
    minApi,
    maxApi,
    mmrlCover,
    needRamdisk,
    mmrlCategory,
    changeBoot,
  } = extra.module;

  const remove = new SuFile(`/data/adb/modules/${id}/remove`);
  const moduleInstalled = new SuFile(`/data/adb/modules/${id}/module.prop`);

  const [moduleRemoved, setModuleRemoved] = React.useState(remove.exist());

  const categories = useFilterCategory(mmrlCategory);
  const { data } = useFetch<string>(notes_url);
  const formatLastUpdate = useFormatDate(last_update);
  const { SupportIcon, supportText } = useSupportIconForUrl(support);

  const handleClickOpen = (scrollType: DialogProps["scroll"]) => () => {
    setOpen(true);
    setScroll(scrollType);
  };

  const handleClose = () => {
    setOpen(false);
  };

  console.log(categories);

  const renderToolbar = () => {
    return (
      <Toolbar modifier="noshadow">
        <Toolbar.Center
          sx={{
            display: "flex",
            justifyContent: "left",
            alignItems: "center",
          }}
        >
          <Typography
            sx={{
              display: "flex",
              justifyContent: "left",
              alignItems: "center",
              fontSize: 20,
            }}
            component="span"
          >
            <CodeRoundedIcon sx={{ mr: 1 }} />
            MMRL
          </Typography>
        </Toolbar.Center>
        <Toolbar.Left>
          <Toolbar.BackButton onClick={context.popPage} />
        </Toolbar.Left>
      </Toolbar>
    );
  };

  return (
    <Page modifier="noshadow" renderToolbar={renderToolbar}>
      <Box
        component="div"
        sx={(theme) => ({
          // pt: 0,
          // pl: 2,
          // pr: 2,
          // pb: 2,
          position: "relative",
          zIndex: 9,
          backgroundColor: theme.palette.primary.main,
          color: "white",
          // display: "flex",
        })}
      >
        {mmrlCover && (
          <Box
            sx={(theme) => ({
              //background: '-webkit-gradient(linear,left bottom,left top,from(rgb(32,33,36)),color-stop(56%,rgba(0,0,0,0)))',
              //background: '-webkit-linear-gradient(bottom,rgb(32,33,36) 0,rgba(0,0,0,0) 56%)',
              background: `linear-gradient(to top,${theme.palette.primary.main} 0,rgba(0,0,0,0) 56%)`,
            })}
          >
            <CardMedia
              component="img"
              sx={(theme) => ({
                zIndex: -1,
                display: "block",
                position: "relative",
                height: {
                  sm: "calc(calc(50vw - 48px)*9/16)",
                  xs: "calc(calc(100vw - 48px)*9/16)",
                },
                objectFit: "cover",
                // width: "calc(100% - 16px)",
              })}
              image={mmrlCover}
              alt={name}
            />
          </Box>
        )}

        <Box
          sx={(theme) => ({
            pt: mmrlCover ? 0 : 2,
            pl: 2,
            pr: 2,
            pb: 2,
            backgroundColor: theme.palette.primary.main,
            color: "white",
            display: "flex",
            flexDirection: "column",
            alignItems: "flex-start",
          })}
        >
          <Box
            sx={{
              display: "flex",
              width: "100%",
            }}
          >
            <Avatar
              alt={name}
              sx={(theme) => ({
                bgcolor: theme.palette.primary.light,
                width: 100,
                height: 100,
                boxShadow: "0 -1px 5px rgba(0,0,0,.09), 0 3px 5px rgba(0,0,0,.06), 0 1px 2px rgba(0,0,0,.3), 0 1px 3px rgba(0,0,0,.15)",
                borderRadius: "20%",
                mr: 1.5,
                fontSize: 50,
              })}
              src={mmrlLogo}
            >
              {name.charAt(0).toUpperCase()}
            </Avatar>

            <Box sx={{ alignSelf: "center", ml: 0.5, mr: 0.5, width: "100%" }}>
              <Typography variant="body1" fontWeight="bold">
                {name}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {mmrlAuthor && authorData ? <span>{authorData.username ? authorData.username : author}</span> : <span>{author}</span>}
                {authorData?.options?.roles?.verified && <VerifiedIcon sx={{ ml: 0.5, fontSize: ".70rem" }} />}
              </Typography>
            </Box>
          </Box>

          <Stack
            sx={{
              mt: 3,
              display: "flex",
              width: "100%",
            }}
            direction={{ xs: "column", sm: "row" }}
            justifyContent="space-between"
            spacing={1}
          >
            <Typography sx={{ display: "flex", justifyContent: "flex-start", alignItems: "center" }} color="text.secondary">
              {version} ({versionCode})
            </Typography>

            <Stack direction={{ xs: "column", sm: "row" }} spacing={1}>
              {support && (
                <Button
                  sx={{
                    color: scheme[100],
                    border: `1px solid ${scheme[100]}80`,
                    minWidth: 160,
                    width: { sm: "unset", xs: "100%" },
                    alignSelf: "flex-end",
                    ":hover": {
                      color: scheme[200],
                      border: `1px solid ${scheme[200]}80`,
                    },
                  }}
                  variant="outlined"
                  startIcon={<SupportIcon />}
                  onClick={() => {
                    os.open(support, {
                      target: "_blank",
                      features: {
                        color: theme.palette.primary.main,
                      },
                    });
                  }}
                >
                  {supportText}
                </Button>
              )}

              <Stack direction="row" justifyContent="center" alignItems="center" spacing={1}>
                {/* {os.isAndroid && moduleInstalled.exist() && (
                  <Button
                    sx={{
                      color: scheme[100],
                      border: `1px solid ${scheme[100]}80`,
                      minWidth: 160,
                      width: { sm: "unset", xs: "100%" },
                      alignSelf: "flex-end",
                      ":hover": {
                        color: scheme[200],
                        border: `1px solid ${scheme[200]}80`,
                      },
                    }}
                    variant="outlined"
                    startIcon={moduleRemoved ? <RefreshRounded /> : <DeleteRounded />}
                    onClick={() => {
                      if (remove.exist()) {
                        setModuleRemoved(remove.delete());
                      } else {
                        setModuleRemoved(!remove.create());
                      }
                    }}
                  >
                    {moduleRemoved ? "Restore" : "Remove"}
                  </Button>
                )} */}

                <Button
                  disabled={!zip_url}
                  onClick={() => {
                    os.open(zip_url, {
                      target: "_blank",
                      features: {
                        color: theme.palette.primary.main,
                      },
                    });
                  }}
                  sx={(theme) => ({
                    bgcolor: scheme[100],
                    ":hover": {
                      bgcolor: scheme[200],
                    },
                    minWidth: 160,
                    width: { sm: "unset", xs: "100%" },
                    alignSelf: "flex-end",
                  })}
                  variant="contained"
                  disableElevation
                >
                  Download
                </Button>
              </Stack>
            </Stack>
          </Stack>
        </Box>
      </Box>

      <Page.RelativeContent>
        <Stack direction="column" justifyContent="center" alignItems="flex-start" spacing={1}>
          {minApi && os.sdk <= Number(minApi) && (
            <Alert
              sx={{
                width: "100%",
              }}
              severity="warning"
            >
              <AlertTitle>Unsupported</AlertTitle>
              Module requires {parseAndroidVersion(minApi)}
            </Alert>
          )}

          {mmrlScreenshots && (
            <Card sx={{ boxShadow: "none", /*width: { xs: "100%", sm: "100vh" },*/ width: "100%" }}>
              <CardContent>
                <Typography variant="h5" component="div">
                  Images
                </Typography>
              </CardContent>

              <ImageList
                sx={{
                  pt: 0,
                  p: 1,
                  overflow: "auto",
                  whiteSpace: "nowrap",
                  gridAutoFlow: "column",
                  gridTemplateColumns: "repeat(auto-fill,minmax(160px,1fr)) !important",
                  gridAutoColumns: "minmax(160px, 1fr)",
                }}
              >
                {mmrlScreenshots.split(",").map((image, i) => (
                  <ImageListItem
                    sx={(theme) => ({
                      ml: 1,
                      mr: 1,
                    })}
                  >
                    <Box
                      component="img"
                      src={image}
                      sx={(theme) => ({
                        boxShadow: "0 1px 2px 0 rgba(60,64,67,.3), 0 1px 3px 1px rgba(60,64,67,.15)",
                        borderRadius: theme.shape.borderRadius / theme.shape.borderRadius,
                      })}
                    />
                  </ImageListItem>
                ))}
              </ImageList>
            </Card>
          )}

          {data ? (
            <Card
              sx={{
                // width: { xs: "100%", sm: "100vh" },

                width: "100%",
                boxShadow: "none",
              }}
            >
              <CardContent>
                <Stack
                  component={Typography}
                  sx={{
                    alignItems: "center",
                  }}
                  direction="row"
                  justifyContent={{ xs: "space-between", sm: "row" }}
                  spacing={1}
                  gutterBottom
                >
                  <Typography variant="h5" component="div">
                    About this module
                  </Typography>
                  <IconButton onClick={handleClickOpen("paper")} sx={{ ml: 0.5 }} aria-label="Example">
                    <ArrowForwardIcon />
                  </IconButton>
                </Stack>

                <Typography
                  component={Markdown}
                  sx={{
                    width: { xs: "50vh" },
                    overflow: "hidden",
                    textOverflow: "ellipsis",
                    display: "-webkit-box",
                    WebkitLineClamp: 5,
                    WebkitBoxOrient: "vertical",
                  }}
                  variant="body2"
                  color="text.secondary"
                  options={{
                    overrides: {
                      h1: {
                        component: "p",
                      },
                      h2: {
                        component: "p",
                      },
                      h3: {
                        component: "p",
                      },
                      h4: {
                        component: "p",
                      },
                      h5: {
                        component: "p",
                      },
                      h6: {
                        component: "p",
                      },
                      img: {
                        component: "p",
                      },
                      video: {
                        component: "p",
                      },
                      audio: {
                        component: "p",
                      },
                      a: {
                        component: "p",
                      },
                    },
                  }}
                >
                  {data}
                </Typography>
                <Typography sx={{ mt: 3 }} variant="h6" component="div">
                  Updated on
                  <Typography sx={{ fontSize: "0.875rem" }} variant="body2" component="div" color="text.secondary">
                    {formatLastUpdate}
                  </Typography>
                </Typography>
                {categories.length !== 0 && (
                  <Box
                    sx={{
                      display: "flex",
                      flexWrap: "wrap",
                      gap: "16px 12px",
                      mt: 3.5,
                    }}
                  >
                    {categories.map((category) => (
                      <Chip label={category} variant="outlined" />
                    ))}
                  </Box>
                )}
              </CardContent>
            </Card>
          ) : null}

          <Card
            sx={{
              // width: { xs: "100%", sm: "100vh" },

              width: "100%",
              boxShadow: "none",
            }}
          >
            <CardContent>
              <Typography variant="h5" component="div">
                Requirements
              </Typography>
            </CardContent>

            <Box
              sx={{
                display: "flex",
                flexDirection: {
                  xs: "column", // mobile
                  sm: "row", // tablet and up
                },
              }}
            >
              <List sx={{ width: { xs: "100%" } }} subheader={<ListSubheader sx={{ bgcolor: "transparent" }}>Access</ListSubheader>}>
                <ListItem>
                  <StyledListItemText primary="Changes boot" secondary={changeBoot === "true" ? "Yes" : "No"} />
                </ListItem>

                <ListItem>
                  <StyledListItemText primary="Needs ramdisk" secondary={needRamdisk === "true" ? "Yes" : "No"} />
                </ListItem>

                <ListItem>
                  <StyledListItemText primary="MMT-Reborn" secondary={mmtReborn === "true" ? "Yes" : "No"} />
                </ListItem>
              </List>

              <List sx={{ width: { xs: "100%" } }} subheader={<ListSubheader sx={{ bgcolor: "transparent" }}>Minimum</ListSubheader>}>
                <ListItem>
                  <StyledListItemText primary="Operating System" secondary={minApi ? parseAndroidVersion(minApi) : "Undefined"} />
                </ListItem>

                <ListItem>
                  <StyledListItemText primary="Magisk" secondary={minMagisk ? Magisk.PARSE_VERSION(minMagisk) : "Undefined"} />
                </ListItem>
              </List>

              <List sx={{ width: { xs: "100%" } }} subheader={<ListSubheader sx={{ bgcolor: "transparent" }}>Recommended</ListSubheader>}>
                <ListItem>
                  <StyledListItemText primary="Operating System" secondary={maxApi ? parseAndroidVersion(maxApi) : "Undefined"} />
                </ListItem>
              </List>
            </Box>
          </Card>
        </Stack>
      </Page.RelativeContent>

      <Dialog
        open={open}
        onClose={handleClose}
        scroll={scroll}
        fullScreen={fullScreen}
        aria-labelledby="scroll-dialog-title"
        aria-describedby="scroll-dialog-description"
      >
        <DialogTitle
          sx={{
            m: 0,
            p: 2,
            display: "flex",
            justifyContent: "left",
            alignItems: "center",
          }}
          id="customized-dialog-title"
        >
          <Box
            sx={{
              display: "flex",
            }}
          >
            <Avatar
              alt={name}
              sx={(theme) => ({
                bgcolor: theme.palette.primary.light,
                width: 56,
                height: 56,
                boxShadow: "0 -1px 5px rgba(0,0,0,.09), 0 3px 5px rgba(0,0,0,.06), 0 1px 2px rgba(0,0,0,.3), 0 1px 3px rgba(0,0,0,.15)",
                borderRadius: "20%",
                mr: 1.5,
                fontSize: 25,
              })}
              src={mmrlLogo}
            >
              {name.charAt(0).toUpperCase()}
            </Avatar>

            <Box sx={{ alignSelf: "center", ml: 0.5, mr: 0.5, width: "100%" }}>
              <Typography variant="body1" fontWeight="bold" noWrap>
                {name}
              </Typography>
              <Typography variant="body2" color="text.secondary" noWrap>
                About this module
              </Typography>
            </Box>
          </Box>
          <IconButton
            aria-label="close"
            onClick={handleClose}
            sx={{
              position: "absolute",
              right: 8,
              color: (theme) => theme.palette.grey[500],
            }}
          >
            <CloseIcon />
          </IconButton>
        </DialogTitle>

        <DialogContent dividers={scroll === "paper"}>
          <DialogContentText id="scroll-dialog-description" tabIndex={-1}>
            <Markup>{data ? data : "Unable to fetch data"}</Markup>
          </DialogContentText>
        </DialogContent>
      </Dialog>
    </Page>
  );
};

interface State {
  data?: string;
  error?: Error;
}

type Cache = { [url: string]: string };

// discriminated union type
type Action = { type: "loading" } | { type: "fetched"; payload: string } | { type: "error"; payload: Error };

export function useFetch<T = unknown>(url?: string, options?: RequestInit): State {
  const cache = React.useRef<Cache>({});

  // Used to prevent state update if the component is unmounted
  const cancelRequest = React.useRef<boolean>(false);

  const initialState: State = {
    error: undefined,
    data: undefined,
  };

  // Keep state logic separated
  const fetchReducer = (state: State, action: Action): State => {
    switch (action.type) {
      case "loading":
        return { ...initialState };
      case "fetched":
        return { ...initialState, data: action.payload };
      case "error":
        return { ...initialState, error: action.payload };
      default:
        return state;
    }
  };

  const [state, dispatch] = React.useReducer(fetchReducer, initialState);

  React.useEffect(() => {
    // Do nothing if the url is not given
    if (!url) return;

    cancelRequest.current = false;

    const fetchData = async () => {
      dispatch({ type: "loading" });

      // If a cache exists for this url, return it
      if (cache.current[url]) {
        dispatch({ type: "fetched", payload: cache.current[url] });
        return;
      }

      try {
        const response = await fetch(url, options);
        if (!response.ok) {
          throw new Error(response.statusText);
        }

        const data = await response.text();
        cache.current[url] = data;
        if (cancelRequest.current) return;

        dispatch({ type: "fetched", payload: data });
      } catch (error) {
        if (cancelRequest.current) return;

        dispatch({ type: "error", payload: error as Error });
      }
    };

    void fetchData();

    // Use the cleanup function for avoiding a possibly...
    // ...state update after the component was unmounted
    return () => {
      cancelRequest.current = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [url]);

  return state;
}

export default ModuleViewActivity;
